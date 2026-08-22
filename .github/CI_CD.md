# CI/CD pipelines

The repository uses three GitHub Actions workflows:

- `ci.yml` validates every pull request and every push to `main`.
- `deploy-backend.yml` runs only after the complete CI workflow succeeds for a push to `main`.
- `deploy-image-server.yml` independently deploys the image service to its own EC2 host after the same CI gate succeeds.

## CI gate

The CI workflow runs these checks in parallel:

| Check | Purpose |
| --- | --- |
| Backend tests | Runs the Java unit, controller, E2E, and PostgreSQL integration tests, excluding the separately executed migration and cross-service suites. |
| Flyway migration tests | Creates the legacy baseline schema in PostgreSQL, applies all pending Flyway migrations, validates the resulting schema with Hibernate, and checks legacy data preservation. |
| Image server tests | Builds the .NET 8 image service and runs its filter, validation, controller, and failure-handling tests. |
| Backend and image-server round trip | Sends a real PNG through the backend post API to a live test-only image-server host, then verifies that the processed URL returns through the backend and is persisted. |
| Backend container build | Confirms the backend production Dockerfile builds. |
| Image-server container build | Confirms the image-server production Dockerfile builds. |
| CI gate | Produces one final success status only when every preceding check succeeds. |

Test reports are uploaded as workflow artifacts, including on failed test runs.

Configure the `main` branch protection rule to require the `CI gate` status check before merging.

## Deployment gate

Both deployment workflows are triggered with `workflow_run`, not directly by `push`. They deploy only when all of the following are true:

1. The completed workflow is `CI`.
2. CI concluded successfully.
3. CI was triggered by a push.
4. The tested branch was `main`.

The workflow checks out and deploys the exact commit SHA validated by CI.

## Required production configuration

The backend deployment uses GitHub OIDC, Amazon ECR, and AWS Systems Manager. The complete setup procedure is documented in the [backend-only EC2 deployment guide](../backend/EC2_DEPLOYMENT.md).

Configure these values in the GitHub `production` environment:

| Type | Name | Description |
| --- | --- | --- |
| Variable | `BACKEND_AWS_ROLE_ARN` | IAM role assumed by GitHub Actions through OIDC. |
| Variable | `BACKEND_AWS_REGION` | AWS Region containing the backend EC2 instance and ECR repository. |
| Variable | `BACKEND_ECR_REPOSITORY` | Private ECR repository used for immutable backend images. |
| Variable | `BACKEND_EC2_INSTANCE_ID` | EC2 instance targeted by the SSM deployment command. |
| Variable | `EC2_APP_DIR` | Absolute path of the repository on EC2. Defaults to `/home/ec2-user/cnytez-springboot`. |
| Secret | `IMAGE_EC2_HOST` | Image-server EC2 DNS name or IPv4 address. |
| Secret | `IMAGE_EC2_USER` | SSH account on the image-server EC2 host. |
| Secret | `IMAGE_EC2_SSH_KEY` | Private SSH key authorized by the image-server EC2 host. |
| Secret | `IMAGE_EC2_KNOWN_HOSTS` | Verified SSH host-key entry for the image-server EC2 host. |
| Variable | `IMAGE_EC2_APP_DIR` | Absolute repository path on the image-server EC2 host. Defaults to `/home/ec2-user/cnytez-springboot`. |

The backend EC2 host must have Git, Docker with Compose, curl, AWS CLI, an active SSM agent, an EC2 instance role with SSM and ECR-pull access, and a clean deployment checkout. Untracked files such as `backend/.env` are allowed; tracked local modifications stop deployment.

The image-server EC2 host must have:

- Git with access to this repository.
- Docker with the Compose plugin.
- A clean deployment checkout. Untracked files such as `backend/.env` are allowed; tracked local modifications stop deployment.

## Health checks

The backend exposes:

- `/actuator/health/liveness` for process liveness.
- `/actuator/health/readiness` for application readiness and PostgreSQL connectivity.

Docker Compose marks the backend healthy only when the readiness endpoint returns `UP`. The deployment script checks this endpoint locally on the EC2 host, which must have `curl` installed. GitHub-hosted runners do not need direct access to port `8080`.

The image server exposes `/health/live` and `/health/ready`, and its Compose service has a container health check. Its deployment script verifies readiness locally over SSH on port `8123`; GitHub Actions therefore does not need direct network access to that port.

## Rollback behavior

The workflow sends `backend/scripts/deploy-with-rollback.sh` to EC2 through an SSM Run Command and executes it with the CI-approved commit SHA and immutable ECR image. The script:

1. Refuses to overwrite tracked modifications on EC2.
2. Records the currently deployed commit.
3. Creates a PostgreSQL custom-format backup when the database is running.
4. Checks out the exact approved commit.
5. Pulls and starts the exact CI-approved backend image without rebuilding it on EC2.
6. Waits up to two minutes for readiness.
7. Restores the previous application commit if deployment stays unhealthy.

Pre-deployment database dumps are stored under `.deployment-backups/` in the EC2 checkout.

Database restoration is deliberately not automatic. Restoring a database is destructive and requires confirming that no production writes would be lost. New Flyway migrations should therefore remain backward compatible with the immediately preceding application version whenever possible.

The image-server workflow copies `image-server/scripts/deploy-with-rollback.sh` to its EC2 instance. It checks out the CI-approved commit, rebuilds only the image-server Compose project, waits for `/health/ready`, and restores the previous application commit if the new container stays unhealthy. It does not touch the backend or PostgreSQL EC2 instance.

## Local verification

Image-server tests:

```bash
cd image-server
dotnet test ImageProcessingServer.Tests/ImageProcessingServer.Tests.csproj --configuration Release
```

Backend tests:

```bash
cd backend
./mvnw clean verify -DexcludedGroups=migration
```

Dedicated Flyway test, which requires Docker:

```bash
cd backend
./mvnw test -Dgroups=migration -Dtest=FlywayMigrationTest
```

Compose validation:

```bash
docker compose -f backend/docker-compose.yml config --quiet
docker compose -f image-server/docker-compose.yml config --quiet
```
