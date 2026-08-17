<div align="center">

# ⚙️ GitHub Actions CI/CD Pipelines

<p align="center">
  Automated Continuous Integration and Continuous Deployment (CI/CD) pipelines for the <strong>Team Cnytez Reddit Backend</strong> platform.
</p>

</div>

---

## 📋 Table of Contents

- [Workflows Overview](#-workflows-overview)
- [Workflow Details](#-workflow-details)
  - [1. Java CI with Maven (`ci.yml`)](#1-java-ci-with-maven-ciyml)
  - [2. Deploy Backend to EC2 (`deploy-backend.yml`)](#2-deploy-backend-to-ec2-deploy-backendyml)
- [Required Secrets & Configuration](#-required-secrets--configuration)
  - [Generating `EC2_KNOWN_HOSTS`](#generating-ec2_known_hosts)
- [Troubleshooting & Common Issues](#-troubleshooting--common-issues)

---

## 📋 Workflows Overview

```
.github/
├── workflows/
│   ├── ci.yml                 # Continuous Integration: Maven build & test execution
│   └── deploy-backend.yml     # Continuous Deployment: Automated AWS EC2 deployment via SSH
└── README.md                  # CI/CD pipelines documentation (this file)
```

| Workflow | File | Trigger(s) | Runner | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Java CI with Maven** | [`ci.yml`](workflows/ci.yml) | • Push to `main`<br>• Pull Request to `main` | `ubuntu-latest` | Compiles Java 25 source code and runs unit, slice, and Testcontainers integration tests. |
| **Deploy Backend to EC2** | [`deploy-backend.yml`](workflows/deploy-backend.yml) | • Push to `main` (`backend/**`)<br>• Manual `workflow_dispatch` | `ubuntu-latest` | Deploys backend updates to the AWS EC2 host via SSH. |

---

## 🔍 Workflow Details

### 1. Java CI with Maven (`ci.yml`)

The CI workflow validates code compilation and runs test suites on every pull request and push to `main`.

#### Key Step Breakdown:
1. **Source Checkout**: `actions/checkout@v4` fetches the repository.
2. **JDK 25 Setup & Dependency Caching**:
   ```yaml
   - name: Set up JDK 25
     uses: actions/setup-java@v4
     with:
       java-version: '25'
       distribution: 'corretto'
       cache: maven
   ```
   Installs Amazon Corretto 25 JDK and caches the local Maven repository (`~/.m2`) to speed up subsequent workflow runs.
3. **Execution Permissions**:
   ```bash
   chmod +x ./mvnw
   ```
   Ensures the Maven wrapper script has executable permissions on Ubuntu runners.
4. **Compile & Test**:
   ```bash
   ./mvnw clean package
   ```
   Executes Maven compilation and runs unit tests, slice tests, and Testcontainers PostgreSQL integration tests inside `backend/`.

---

### 2. Deploy Backend to EC2 (`deploy-backend.yml`)

The CD workflow triggers automated deployment over SSH when changes to the backend are merged into `main`.

#### Key Step Breakdown:
1. **Path-Based Triggering**:
   ```yaml
   on:
     push:
       branches: [ main ]
       paths:
         - "backend/**"
         - ".github/workflows/deploy-backend.yml"
     workflow_dispatch:
   ```
   Filters workflow execution so deployments only run when files within `backend/` or the workflow itself are modified.
2. **Concurrency Queue**:
   ```yaml
   concurrency:
     group: backend-production
     cancel-in-progress: false
   ```
   Queues deployments sequentially (`cancel-in-progress: false`) rather than canceling active runs, preventing corrupted state or partially applied database migrations.
3. **SSH Setup & Host Verification**:
   ```bash
   install -m 700 -d ~/.ssh
   printf '%s\n' "$EC2_SSH_KEY" > ~/.ssh/ec2_key
   chmod 600 ~/.ssh/ec2_key
   printf '%s\n' "$EC2_KNOWN_HOSTS" > ~/.ssh/known_hosts
   chmod 600 ~/.ssh/known_hosts
   ```
   Restricts private key file permissions (`chmod 600`) and registers the host key fingerprint to prevent MITM attacks.
4. **Remote Deployment Invocation**:
   ```bash
   ssh \
     -i ~/.ssh/ec2_key \
     -o BatchMode=yes \
     -o ConnectTimeout=15 \
     "$USER@$HOST" \
     "/home/ec2-user/deploy-backend.sh"
   ```
   Connects non-interactively to the EC2 host and executes `/home/ec2-user/deploy-backend.sh`, which pulls the latest code and restarts the container stack with Docker Compose.

---

## 🔐 Required Secrets & Configuration

Configure these secrets under **Settings** > **Secrets and variables** > **Actions** (or in the `production` environment):

| Secret Name | Required | Example / Description |
| :--- | :---: | :--- |
| `EC2_HOST` | **Yes** | `ec2-18-193-138-107.eu-central-1.compute.amazonaws.com` (or public IPv4) |
| `EC2_USER` | **Yes** | `ec2-user` (Amazon Linux) or `ubuntu` (Ubuntu Server) |
| `EC2_SSH_KEY` | **Yes** | Private SSH Key in OpenSSH format (`-----BEGIN OPENSSH PRIVATE KEY-----...`) |
| `EC2_KNOWN_HOSTS` | **Yes** | SSH host public key entry for host verification |

---

### Generating `EC2_KNOWN_HOSTS`

To generate the host fingerprint for host key verification:

```bash
ssh-keyscan -H <EC2_HOST>
```

Paste the resulting output string into the `EC2_KNOWN_HOSTS` secret in your GitHub repository.

---

## 🔧 Troubleshooting & Common Issues

### 1. `Permission denied (publickey)`
* **Cause**: `EC2_SSH_KEY` does not match the public key in `/home/ec2-user/.ssh/authorized_keys` on the EC2 instance, or `EC2_USER` is incorrect.
* **Fix**: Ensure the private key matches the instance's authorized keys and that `EC2_USER` matches the instance OS (e.g. `ec2-user` for Amazon Linux, `ubuntu` for Ubuntu).

### 2. `Host key verification failed`
* **Cause**: `EC2_KNOWN_HOSTS` is missing or out of date.
* **Fix**: Re-run `ssh-keyscan -H <EC2_HOST>` and update the `EC2_KNOWN_HOSTS` secret in GitHub.

### 3. `./mvnw: /bin/sh^M: bad interpreter: No such file or directory`
* **Cause**: The Maven wrapper script has Windows CRLF line endings.
* **Fix**: The repository includes [`.gitattributes`](../.gitattributes) with `/mvnw text eol=lf`. Ensure this file is committed and run `git add --renormalize .` if needed.

### 4. Deployment Job Queued Indefinitely
* **Cause**: A previous deployment is still running or locked by the concurrency group `backend-production`.
* **Fix**: Check active workflow runs in GitHub Actions and cancel stalled runs if necessary.
