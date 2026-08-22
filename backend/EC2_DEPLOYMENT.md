# Backend-only deployment on Amazon EC2

This guide covers the current backend deployment: Spring Boot and PostgreSQL on one Amazon Linux 2023 EC2 instance, images stored in ECR, and deployments sent through AWS Systems Manager (SSM).

The image-processing server is outside the scope of this guide. This workflow does not connect to, restart, or update the image-server EC2 instance. The backend can still call an existing image-processing server through the URL configured by the application.

The current shared CI workflow still runs the image-server tests and the backend-to-image-server contract test. Those checks gate the backend deployment, but passing them does not deploy the image server.

## Deployment flow

1. Pull requests must pass the complete `CI` workflow before merge.
2. CI runs again after a push to `main`. If it passes, the backend deployment starts.
3. GitHub assumes an AWS role through OIDC. There are no long-lived AWS keys in GitHub.
4. The tested commit is built and pushed to ECR, tagged with its full commit SHA.
5. GitHub sends the deployment command to EC2 through SSM.
6. EC2 backs up PostgreSQL, pulls the image, replaces only the backend container, and checks readiness.
7. If readiness fails, the script starts the previous image again. It does not restore the database automatically.

## Current choices and alternatives

The commands in this guide use the current deployment as the example:

| Setting | Current choice | If using something else |
| --- | --- | --- |
| AWS account | `386807259133` | Replace the account ID in IAM ARNs, the ECR registry, the workflow, and the deployment script. |
| AWS Region | `us-east-1` | Keep ECR and EC2 in the chosen Region, then replace every `us-east-1` reference. |
| EC2 operating system | Amazon Linux 2023, x86_64 | Use the installation commands and SSM Agent package for the chosen operating system and architecture. |
| ECR repository | `cnytez-backend` | Replace the repository name in ECR, IAM, GitHub variables, the workflow, and the deployment script. |
| ECR registry | `386807259133.dkr.ecr.us-east-1.amazonaws.com` | Build it as `<account-id>.dkr.ecr.<region>.amazonaws.com`. |
| EC2 application directory | `/home/ec2-user/cnytez-springboot` | Update `EC2_APP_DIR` and any fixed paths or commands that use the current directory. |
| EC2 user | `ec2-user` | Update the workflow's `runuser`, ownership command, home directory, and application path. |
| GitHub repository | `Matei5/cnytez-springboot` | Update the clone URL and OIDC trust subject. |
| GitHub environment | `production` | Use the same environment name in the workflow and OIDC trust subject. |
| Release branch | `main` | Update the CI and deployment triggers, branch checks, initial checkout, and deployment script fetch command. |
| Backend port | `8080` | Update Compose, health checks, the deployment script, and the security group. |
| PostgreSQL database and user | `cnytez_db`, `postgres` | Update Compose, the datasource settings, health check, and backup command together. |

Use the ID of the new instance when setting up a replacement server.

Several safety checks are fixed to the current production values. When using another AWS account, Region, repository, or EC2 instance, update all of these together:

- `.github/workflows/deploy-backend.yml`: release branch, environment, allowed AWS account, expected registry, repository, Region, instance ID, EC2 user, home directory, and default application directory.
- `backend/scripts/deploy-with-rollback.sh`: release branch, ECR registry, repository, Region, backend health URL, PostgreSQL user, and database name.
- `backend/docker-compose.yml`: published backend port, PostgreSQL user and database, health checks, and application environment.
- The IAM policy ARNs and GitHub environment variables described below.

Changing only the GitHub variables is not enough. The workflow will stop when a value does not match its safety check.

## 1. Create the ECR repository

The current repository is `cnytez-backend` in `us-east-1`. In **Amazon ECR → Private registry → Repositories**, create it and select tag immutability. Another repository name or Region also works when the workflow, deployment script, IAM policies, and GitHub variables use the same values.

Enable image scanning and add a lifecycle rule for old images. Ten recent releases is enough to keep several rollback options without letting storage grow indefinitely. Each deployed tag is a full Git commit SHA.

## 2. Launch and prepare EC2

The current server is an x86_64 Amazon Linux 2023 instance in `us-east-1`. Another Region can be used, and another Linux distribution can be used if it supports Docker, AWS CLI v2, and SSM Agent. Adjust the package commands accordingly.

Its security group normally needs:

- TCP `8080` from only the clients or load balancer that must reach the API.
- TCP `22` from your IP during initial setup, if you use SSH.
- No inbound rule for SSM; the agent connects outbound over HTTPS.

The instance needs outbound HTTPS access to GitHub, ECR, and SSM. A private instance needs NAT access or the required VPC endpoints.

Port `8080` serves plain HTTP. Put an HTTPS load balancer or reverse proxy in front of it before exposing the API to Internet clients. PostgreSQL port `5432` does not need an inbound rule.

Connect to the instance and install the required software:

```bash
sudo dnf update -y
sudo dnf install -y git docker curl
```

AWS Amazon Linux 2023 AMIs normally include SSM Agent. If `command -v amazon-ssm-agent` returns nothing, install the package matching the instance Region and architecture. For the current x86_64 instance in `us-east-1`:

```bash
sudo dnf install -y \
  https://s3.us-east-1.amazonaws.com/amazon-ssm-us-east-1/latest/linux_amd64/amazon-ssm-agent.rpm
```

For ARM64, replace `linux_amd64` with `linux_arm64`. For another Region, replace both `us-east-1` occurrences in the URL.

Start the services and grant `ec2-user` access to Docker:

```bash
sudo systemctl enable --now docker
sudo systemctl enable --now amazon-ssm-agent
sudo usermod -aG docker ec2-user
```

Sign out and reconnect so the Docker group change takes effect. Then verify:

```bash
git --version
docker --version
docker compose version
curl --version
aws --version
sudo systemctl is-active amazon-ssm-agent
```

Amazon Linux 2023 includes AWS CLI v2. If `docker compose` is unavailable, install the Docker Compose plugin before continuing.

## 3. Attach an EC2 instance role

Create an IAM role trusted by `ec2.amazonaws.com`. The current role is named `CnytezBackendEc2Role`, but the name can be changed. Attach:

- AWS-managed policy `AmazonSSMManagedInstanceCore`.
- A least-privilege ECR pull policy for this repository:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:BatchGetImage",
        "ecr:GetDownloadUrlForLayer"
      ],
      "Resource": "arn:aws:ecr:us-east-1:386807259133:repository/cnytez-backend"
    }
  ]
}
```

Attach the role to the EC2 instance. Confirm the instance identity and ECR access. The login command below uses the current Region and registry; replace both for another installation:

```bash
aws sts get-caller-identity
aws ecr get-login-password --region us-east-1 |
  docker login --username AWS --password-stdin \
  386807259133.dkr.ecr.us-east-1.amazonaws.com
```

The instance should now appear as an online managed node in **Systems Manager → Fleet Manager**. If it is missing, check the instance role, agent, selected Region, and outbound access.

## 4. Create the initial EC2 checkout

Clone the repository into the path used by the workflow. The commands below use the current public repository, `ec2-user`, and application directory:

```bash
cd /home/ec2-user
git clone https://github.com/Matei5/cnytez-springboot.git
cd /home/ec2-user/cnytez-springboot
git checkout main
```

The current repository is public, so EC2 can fetch it without a GitHub credential. A private repository needs a read-only deploy key or another non-interactive read credential on EC2 before automated deployments can fetch commits.

The deployment script refuses to overwrite tracked local modifications. Keep production-only configuration in ignored files, not tracked files.

Create `/home/ec2-user/cnytez-springboot/backend/.env`:

```env
POSTGRES_PASSWORD=replace_with_a_long_random_database_password
JWT_SECRET=replace_with_a_long_random_jwt_secret
```

For example, run `openssl rand -hex 32` twice on a trusted machine and use a different result for each value. Hex output avoids characters that `.env` might interpret.

Restrict access to it:

```bash
chmod 600 /home/ec2-user/cnytez-springboot/backend/.env
```

Generate both values with a cryptographically secure random generator. Keep the file on EC2 and out of Git. Changing `JWT_SECRET` later invalidates existing login tokens; changing `POSTGRES_PASSWORD` in this file alone does not update the password already stored by PostgreSQL.

Changes to the image-server URL should go through the repository and CI, not through a tracked-file edit on EC2.

## 5. Start the stack once

The first automated deployment needs an existing image to use as its rollback target. Build and start the backend and database once from the checkout:

```bash
cd /home/ec2-user/cnytez-springboot/backend
docker compose config --quiet
docker compose up --detach --build
```

Wait for both containers and verify readiness:

```bash
docker compose ps
curl --fail --silent --show-error \
  http://localhost:8080/actuator/health/readiness
echo
```

Continue only when `reddit-db` and `reddit-backend` are healthy and the endpoint returns `{"status":"UP"}`.

On an empty PostgreSQL volume, Compose loads the baseline schema and Spring Boot applies the remaining Flyway migrations when the backend starts. On an existing volume, Flyway applies only migrations that have not run yet.

Do not run `docker compose down --volumes` in production. The `postgres_data` volume contains the database.

## 6. Configure GitHub OIDC

In AWS IAM, add the OpenID Connect provider:

- Provider URL: `https://token.actions.githubusercontent.com`
- Audience: `sts.amazonaws.com`

Create a GitHub deployment role with `sts:AssumeRoleWithWebIdentity`. The current role is `CnytezGitHubBackendPublisherRole`, but its name can be changed. Restrict its trust policy to the intended repository and protected GitHub environment. The current repository uses the immutable GitHub owner and repository IDs in its `sub` claim:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::386807259133:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
          "token.actions.githubusercontent.com:sub": "repo:Matei5@147913718/cnytez-springboot@1307634229:environment:production"
        }
      }
    }
  ]
}
```

For a fork, another repository, or another GitHub environment, replace the subject with the identity issued for that combination. Replace the AWS account ID in the provider ARN when using another AWS account.

Give this role only the permissions required to push to `cnytez-backend` and send/read a command for the selected EC2 instance. The SSM policy can be restricted as follows, replacing the instance ID:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ssm:SendCommand",
      "Resource": [
        "arn:aws:ssm:us-east-1::document/AWS-RunShellScript",
        "arn:aws:ec2:us-east-1:386807259133:instance/REPLACE_WITH_INSTANCE_ID"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "ssm:GetCommandInvocation",
      "Resource": "*"
    }
  ]
}
```

Attach this ECR publisher policy to the GitHub role:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:CompleteLayerUpload",
        "ecr:DescribeImages",
        "ecr:GetDownloadUrlForLayer",
        "ecr:InitiateLayerUpload",
        "ecr:PutImage",
        "ecr:UploadLayerPart"
      ],
      "Resource": "arn:aws:ecr:us-east-1:386807259133:repository/cnytez-backend"
    }
  ]
}
```

## 7. Configure the GitHub production environment

Create the `production` environment under **Repository settings → Environments**. This is the environment used by the current workflow. Another name works if both the workflow and OIDC trust subject are updated to match it. Allow deployments only from `main`, or replace `main` consistently in the CI workflow, deployment workflow, deployment script, and initial checkout. Add required reviewers if releases need manual approval.

Add these environment variables:

| Name | Value |
| --- | --- |
| `BACKEND_AWS_ROLE_ARN` | ARN of the GitHub OIDC deployment role |
| `BACKEND_AWS_REGION` | `us-east-1` |
| `BACKEND_ECR_REPOSITORY` | `cnytez-backend` |
| `BACKEND_EC2_INSTANCE_ID` | ID of the backend EC2 instance |
| `EC2_APP_DIR` | `/home/ec2-user/cnytez-springboot` |

The SSM backend workflow does not require `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, or `EC2_KNOWN_HOSTS`. Values prefixed with `IMAGE_EC2_` belong to the separate image-server deployment and must not be removed if that workflow still uses them.

Protect `main` and require the `CI gate` check before merging. The deployment workflow must retain `id-token: write` and `contents: read` permissions so OIDC and checkout work.

## 8. Run and verify the first automated deployment

Merge a tested pull request into `main`. In GitHub Actions, verify that CI succeeds before **Deploy backend to EC2** starts. Its deployment step should report SSM status `Success` and response code `0`.

On EC2, verify that the running image, readiness response, and release markers all identify the same commit:

```bash
docker inspect reddit-backend \
  --format 'image={{.Config.Image}} status={{.State.Status}} health={{.State.Health.Status}}'

curl --fail --silent --show-error \
  http://localhost:8080/actuator/health/readiness
echo

cat /home/ec2-user/cnytez-springboot/.last-successful-backend-sha
cat /home/ec2-user/cnytez-springboot/.last-successful-backend-image
```

The image tag and both marker files must show the commit deployed by GitHub Actions.

Also open **Systems Manager → Run Command → Command history** and confirm that the `AWS-RunShellScript` invocation targeted the expected instance and finished with `Success`.

Backend readiness checks the application and PostgreSQL. It does not check whether the separately deployed image-processing server is available.

## Operations and recovery

- Database backups are written to `/home/ec2-user/cnytez-springboot/.deployment-backups/` before deployments. Copy or rotate them according to the project's retention requirements; local-only backups do not protect against instance loss.
- Application rollback is automatic when the new backend fails readiness. A database restore is never automatic because it can discard production writes.
- The rollback script can be run manually with `/home/ec2-user/cnytez-springboot/backend/scripts/deploy-with-rollback.sh --rollback /home/ec2-user/cnytez-springboot`. It uses the `.previous-backend-image` and `.previous-backend-sha` marker files from the last deployment.
- Use Flyway for schema changes and keep migrations compatible with the immediately previous backend version whenever possible.
- Monitor free disk space. ECR lifecycle rules do not delete EC2 database backups, Docker volumes, or container logs.
- Test **Systems Manager → Session Manager → Start session** before removing SSH access. A successful Run Command does not by itself prove that administrators can open an interactive Session Manager shell.
- After interactive SSM access is verified, port `22` can be removed from the security group if SSH is no longer needed.
- Do not expose PostgreSQL port `5432` publicly.

Useful diagnostics:

```bash
sudo systemctl is-active amazon-ssm-agent
docker compose -f /home/ec2-user/cnytez-springboot/backend/docker-compose.yml ps
docker logs --tail 200 reddit-backend
docker logs --tail 200 reddit-db
git -C /home/ec2-user/cnytez-springboot status --short
```
