<div align="center">

# 🌐 Team Cnytez's Reddit Backend

[![Java 25](https://img.shields.io/badge/Java-25-orange.svg?logo=openjdk)](https://docs.aws.amazon.com/corretto/latest/corretto-25-ug/downloads-list.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Database%20Migrations-red.svg?logo=flyway)](https://flywaydb.org/)
[![.NET](https://img.shields.io/badge/.NET-8.0-purple.svg?logo=dotnet)](https://dotnet.microsoft.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker)](https://www.docker.com/)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF.svg?logo=githubactions)](.github/README.md)
[![AWS EC2](https://img.shields.io/badge/Deploy-AWS%20EC2-FF9900.svg?logo=amazonec2)](https://aws.amazon.com/ec2/)

<p align="center">
  A containerized REST API and microservice platform for <strong>Team Cnytez's Reddit Backend</strong>, built as part of the <strong>Cognyte Zero to Hero (ZtH)</strong> program.
</p>

<p align="center">
  This repository encompasses the <strong>backend services only</strong>. The core objective was to design, develop, test, and deploy a backend architecture tailored specifically to satisfy the API contracts, data models, and feature requirements defined by the <a href="https://zth-cog-fe.netlify.app/api-docs">Frontend Client API Specifications</a>.
</p>

</div>

---

## 📋 Table of Contents

- [System Architecture](#-system-architecture)
- [Key Features](#-key-features)
- [Repository Structure](#-repository-structure)
- [Technology Stack](#-technology-stack)
- [Quick Start & Local Setup](#-quick-start--local-setup)
  - [Prerequisites](#1-prerequisites)
  - [Environment Configuration](#2-environment-configuration)
  - [Running the Services](#3-running-the-services)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [API Quick Reference](#-api-quick-reference)
- [CI/CD & Deployment Overview](#-cicd--deployment-overview)

---

## 🏗 System Architecture

The application is architected as microservices running across AWS EC2 instances:

1. **Spring Boot Backend (Primary EC2 Instance)**
   - **Core REST API**: Built on Java 25 and Spring Boot, running as a containerized service handling user authentication (OAuth2 / JWT), subreddit management, posts, comments, voting mechanics, and business logic.
   - **Database Layer**: A containerized PostgreSQL 15 database running alongside the backend on the same instance, with automated Flyway schema migrations.

2. **Image Processing & Storage Server (Secondary EC2 Instance)**
   - **Processing Microservice**: An ASP.NET Core service running on a separate EC2 instance, responsible for image transformations and visual filter pipelines (e.g., Grayscale, Sepia, Inverted, Blur, Pixelated).
   - **AWS S3 Storage**: Processed images are uploaded and stored on Amazon S3.

3. **Inter-Service Communication**
   - Client applications interact directly with the Spring Boot backend via HTTP/REST JSON endpoints.
   - The Spring Boot backend orchestrates media operations by issuing HTTP REST requests to the Image Processing Microservice on the second EC2 instance.

---

## ✨ Key Features

- **Authentication & Authorization**: Stateless JWT security leveraging Spring Security with OAuth2 Resource Server support and BCrypt password hashing.
- **Subreddit Communities**: Topic-based subreddit creation, moderation metadata, and post grouping.
- **Posts & Voting System**: Text posts and image posts with upvote/downvote mechanics, aggregate score calculations, and sorting.
- **Discussion & Comments**: Nested discussion threads with associated author metadata.
- **Image Processing**: Integration with a .NET 8 microservice for applying server-side visual image filters (e.g., Grayscale, Inverted, Sepia, Blur, Pixelated).
- **Database Evolution (Flyway)**: Versioned database migration scripts ensuring consistent schema evolution across local and production databases.
- **Test Suite**: Unit, Integration, and E2E testing powered by JUnit 5 and Testcontainers PostgreSQL.
- **Automated CI/CD**: Automated build, test, and deployment to AWS EC2 via GitHub Actions SSH pipelines.

---

## 📂 Repository Structure

```
.
├── .github/                     # CI/CD workflows and deployment documentation
│   ├── workflows/               # GitHub Actions CI & CD pipeline definitions
│   └── README.md                # CI/CD documentation
│
├── backend/                     # Spring Boot 4 REST API service
│   ├── src/                     # Java source code, configs, and migration scripts
│   ├── Dockerfile               # Multi-stage container build definition
│   ├── docker-compose.yml       # Local backend & PostgreSQL stack orchestration
│   ├── pom.xml                  # Maven dependencies & build plugins
│   └── README.md                # Backend architecture & setup documentation
│
├── image-server/                # .NET 8 Image Processing Microservice
│   ├── ImageProcessingServer/   # ASP.NET Core source code & filter logic
│   ├── Dockerfile               # .NET 8 container build definition
│   ├── docker-compose.yml       # Standalone image service container config
│   └── README.md                # Image processing microservice documentation
│
├── .gitattributes               # Line-ending normalization (LF for mvnw)
├── .gitignore                   # Git ignore rules for Maven, IDEs, and environments
└── README.md                    # Root project documentation (this file)
```

---

## 🛠 Technology Stack

| Domain | Technology | Purpose & Details |
| :--- | :--- | :--- |
| **Core Backend** | **Java 25 & Spring Boot 4.1.0** | REST API framework, Spring MVC, Jakarta Validation |
| **Security & Auth**| **Spring Security & OAuth2** | Stateless JWT bearer tokens, BCrypt hashing, role/route guards |
| **ORM & Persistence**| **Spring Data JPA & Hibernate** | Object-Relational Mapping, connection pooling, repository layer |
| **Database** | **PostgreSQL 15 (Alpine)** | Relational database |
| **Database Migrations**| **Flyway** | Versioned database schema migrations |
| **Image Service** | **.NET 8 (C#) / ASP.NET Core** | Microservice for image transformations and visual filters |
| **Object Mapping** | **MapStruct 1.6.3 & Lombok** | Compile-time entity <-> DTO mapping and boilerplate reduction |
| **Testing** | **JUnit 5, Mockito, Testcontainers** | Unit, slice, and PostgreSQL containerized integration testing |
| **Containerization**| **Docker & Docker Compose** | Docker packaging and local microservice orchestration |
| **CI/CD** | **GitHub Actions** | CI pipeline (build/test) and CD pipeline (SSH deploy to EC2) |
| **Cloud Hosting** | **AWS EC2** | Cloud container host |

---

## 🚀 Quick Start & Local Setup

### 1. Prerequisites
Ensure you have the following installed on your machine:
- [Java 25 JDK](https://docs.aws.amazon.com/corretto/latest/corretto-25-ug/downloads-list.html) (Amazon Corretto 25 recommended)
- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0) *(optional, for running image service outside Docker)*
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) *(must be running)*
- Git

---

### 2. Environment Configuration

Create a `.env` file in the [`backend/`](backend) directory:

```env
POSTGRES_PASSWORD=your_secure_password
JWT_SECRET=your_base64_or_hex_jwt_secret_key
```

---

### 3. Running the Services

The application architecture consists of the **Spring Boot Backend + PostgreSQL Database** stack and the **.NET Image Processing Microservice**.

#### ☁️ Production Deployment (2 Separate AWS EC2 Instances)
In the live production environment, the services run on two physically separate EC2 instances:
- **EC2 Instance 1**: Runs `backend/docker-compose.yml` (Spring Boot API + PostgreSQL).
- **EC2 Instance 2**: Runs `image-server/docker-compose.yml` (ASP.NET Core image processing service).
- **Cross-Host Networking**: Because the services run on distinct cloud hosts, the Spring Boot backend reaches the image processing service over HTTP via the public EC2 DNS/IP configured in `application.yaml`:
  `http://ec2-18-193-138-107.eu-central-1.compute.amazonaws.com:8123`

---

#### 💻 Local Development Setup (Single Machine with Docker)
When developing on a local machine, you have two options:

##### Mode 1: Local Backend with Remote Image Server (Default)
Spins up the local backend and PostgreSQL database. Image processing requests are routed across the internet to the hosted EC2 image server:

```bash
cd backend
docker compose up --build -d
```
* **Backend API**: `http://localhost:8080`
* **PostgreSQL**: `localhost:5432`

##### Mode 2: Fully Local Multi-Container Stack (Shared Docker Network)
Because `backend/` and `image-server/` use separate `docker-compose.yml` files, Docker places them on isolated bridge networks by default (`backend_default` and `image-server_default`). 

To run both stacks locally without calling the remote AWS EC2 instance, join both containers onto a shared Docker network:

1. **Create a shared Docker network**:
   ```bash
   docker network create cnytez-network
   ```

2. **Start the Image Processing Server** on the shared network:
   ```bash
   cd image-server
   docker compose up --build -d
   docker network connect cnytez-network image-server
   ```

3. **Start the Backend Stack** on the shared network and point to the local container:
   ```bash
   cd ../backend
   docker compose up --build -d
   docker network connect cnytez-network reddit-backend
   ```
   > Set `image-server.url: http://image-server:8123` under `services.backend.environment` in `backend/docker-compose.yml` (or via environment variable) so Spring Boot routes image filter requests directly to the local container.

---

## 🧪 Testing & Quality Assurance

The backend repository includes unit tests, web MVC slice tests, and integration tests using **Testcontainers**:

```bash
cd backend

# Run all test suites
./mvnw test

# Run only Controller unit tests
./mvnw test -Dtest="*ControllerTest"

# Run integration tests (spins up temporary PostgreSQL via Testcontainers)
./mvnw test -Dtest="*IntegrationTest"

# Run end-to-end tests
./mvnw test -Dtest="*E2ETest"

# Run an individual test class
./mvnw test -Dtest="PostServiceTest"

# Run an individual test method
./mvnw test -Dtest="PostServiceTest#getPostById_Found_ReturnsPostDto"
```

*Note: Ensure Docker Desktop is running before executing `./mvnw test`, as Testcontainers requires Docker to spin up temporary PostgreSQL database instances.*

---

## 📡 API Quick Reference

| Module | Method | Endpoint | Access | Summary |
| :---: | :---: | :--- | :---: | :--- |
| **Health** | `GET` | `/health` | Public | System status and database connectivity check |
| **Auth** | `POST` | `/api/auth/register` | Public | Register a new user account |
| **Auth** | `POST` | `/api/auth/login` | Public | Authenticate user & receive JWT access token |
| **Subreddits** | `GET` | `/api/subreddits` | Public | List all available subreddits |
| **Subreddits** | `POST` | `/api/subreddits` | Authenticated | Create a new subreddit community |
| **Posts** | `GET` | `/api/posts` | Public | Fetch post feed (supports pagination/sorting) |
| **Posts** | `GET` | `/api/posts/{id}` | Public | Retrieve a specific post by ID |
| **Posts** | `POST` | `/api/posts` | Authenticated | Create a text or image post |
| **Posts** | `POST` | `/api/posts/{id}/vote` | Authenticated | Upvote or downvote a post |
| **Comments** | `GET` | `/api/posts/{id}/comments` | Public | Retrieve all comments for a post |
| **Comments** | `POST` | `/api/posts/{id}/comments` | Authenticated | Post a comment on a submission |
| **Filters** | `GET` | `/api/filters` | Public | List available visual image filters |

For complete JSON request payloads, response schemas, and interactive testing, refer to the [API Specification](https://zth-cog-fe.netlify.app/api-docs).

---

## 🔄 CI/CD & Deployment Overview

Automated pipelines are implemented using **GitHub Actions**:

1. **Continuous Integration ([`ci.yml`](.github/workflows/ci.yml))**:
   - Triggers on every push and PR targeting the `main` branch.
   - Sets up Amazon Corretto 25 JDK, caches Maven dependencies, and executes `./mvnw clean package`.

2. **Continuous Deployment ([`deploy-backend.yml`](.github/workflows/deploy-backend.yml))**:
   - Triggers automatically when backend code is merged to `main`, or via manual `workflow_dispatch`.
   - Utilizes SSH with host key verification (`known_hosts`) to connect to the AWS EC2 instance.
   - Executes remote deployment scripts to pull the latest code and rebuild and restart containers with Docker Compose.
