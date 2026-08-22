<div align="center">

# 🚀 Team Cnytez Reddit Backend (API)

<p align="center">
  Spring Boot REST API for the Reddit clone backend (Cognyte ZtH), implementing the <a href="https://zth-cog-fe.netlify.app/api-docs">Frontend Client API Specification</a>.
</p>

</div>

---

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Service Structure & Packages](#-service-structure--packages)
- [Database & Migrations](#-database--migrations)
- [Configuration & Environment Variables](#-configuration--environment-variables)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Running with Docker Compose](#running-with-docker-compose)
- [Testing](#-testing)
- [API Endpoints Overview](#-api-endpoints-overview)
- [Containerization](#-containerization)
- [Production deployment on EC2](EC2_DEPLOYMENT.md)

---

## 🛠 Tech Stack

- **Runtime & Language**: Java 25 (Amazon Corretto)
- **Framework**: Spring Boot 4.1.0
  - `spring-boot-starter-web`: RESTful controllers and HTTP pipeline
  - `spring-boot-starter-data-jpa`: Hibernate & JPA ORM
  - `spring-boot-starter-security`: Spring Security authentication & authorization
  - `spring-boot-starter-oauth2-resource-server`: JWT validation and token decoding
  - `spring-boot-starter-validation`: Bean Validation (Jakarta Validation)
  - `spring-boot-starter-flyway`: Automated database migrations
- **Database**: PostgreSQL 15 (Alpine)
- **Mapping & Utilities**: MapStruct 1.6.3, Project Lombok
- **Testing**: JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL), Jayway JsonPath, H2 (In-memory)

---

## 📂 Service Structure & Packages

The backend follows a layered, domain-centric architecture under `com.cnytez.app`:

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/cnytez/app/
│   │   │   ├── config/          # Spring & Security configuration (JWT, Security filter chain, logging)
│   │   │   │   ├── AppConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── LoggingConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/      # REST API Controllers (HTTP request handlers)
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CommentController.java
│   │   │   │   ├── FilterController.java
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── PostController.java
│   │   │   │   └── SubredditController.java
│   │   │   ├── dto/             # Request & Response Data Transfer Objects
│   │   │   ├── exception/       # Custom exceptions and global @RestControllerAdvice handlers
│   │   │   ├── logging/         # AOP / interceptors for structured request/response logging
│   │   │   ├── mapper/          # MapStruct interfaces for Entity <-> DTO conversion
│   │   │   ├── model/           # JPA Entities (User, Post, Comment, Subreddit, Filter, PostVote, CommentVote)
│   │   │   ├── repository/      # Spring Data JPA interfaces
│   │   │   ├── service/         # Business logic and transaction orchestration
│   │   │   └── AppApplication.java # Application entry point
│   │   └── resources/
│   │       ├── application.yaml # Application configuration with env overrides
│   │       └── db/migration/    # Flyway SQL migration scripts
│   └── test/
│       └── java/com/cnytez/app/
│           ├── controller/      # Web MVC slice & controller unit tests
│           ├── service/         # Service layer unit tests (Mockito)
│           ├── integration/     # Testcontainers PostgreSQL integration tests
│           └── e2e/             # Full end-to-end API workflow tests
├── Dockerfile                   # Multi-stage build (Corretto 25 -> Alpine JRE)
├── docker-compose.yml           # Backend API + PostgreSQL 15 local stack
├── pom.xml                      # Maven dependencies & build plugins
└── BACKEND.md                   # Backend documentation (this file)
```

---

## 🗄 Database & Migrations

Database schema evolution is managed via **Flyway**:
- **Baseline Configuration**: `spring.flyway.baseline-on-migrate: true` (baseline version `1`).
- **Migration Scripts**: Located in `src/main/resources/db/migration/` (versioned `V2+` SQL scripts applied sequentially on top of the baseline schema).
- **Hibernate DDL Auto**: Set to `validate` (`spring.jpa.hibernate.ddl-auto: validate`), ensuring Flyway is the single source of truth for schema changes.

---

## ⚙ Configuration & Environment Variables

The application is configured through `src/main/resources/application.yaml` with environment variable overrides.

Create a `.env` file in the `backend/` directory for local docker execution:

```env
# backend/.env
POSTGRES_PASSWORD=your_db_password_here
JWT_SECRET=your_base64_or_secure_jwt_secret_key_here
```

### Key Environment Variables

| Variable | Description | Default / Local Fallback |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC connection URL for PostgreSQL | `jdbc:postgresql://localhost:5432/reddit` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL database user | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL database password | `1234` |
| `JWT_SECRET` | Secret key used for signing & verifying JWT tokens | *(Required)* |
| `image-server.url` | Base URL of the .NET image processing service | `http://ec2-18-193-138-107.eu-central-1.compute.amazonaws.com:8123` |

---

## 🚀 Getting Started

### Prerequisites

- **Docker Desktop** (required for containerized execution and Testcontainers)
- **Java 25 JDK** (for compiling or running test suites with `./mvnw test`)

### Running with Docker Compose

#### Production Deployment vs. Local Setup
- **Production (2 EC2 Instances)**: The backend stack (`backend/docker-compose.yml`) runs on the primary EC2 instance, while the image server (`image-server/docker-compose.yml`) runs on a separate EC2 instance. In production, inter-service traffic travels across AWS over HTTP to `http://ec2-18-193-138-107.eu-central-1.compute.amazonaws.com:8123`.
- **Local Development**:
  - **Default**: Running the backend compose stack connects to the database locally while forwarding image processing calls to the hosted EC2 image server.
  - **Shared Local Network (Option 2)**: If you run `image-server` locally as well, connect both container stacks to a shared bridge network (`docker network create cnytez-network && docker network connect cnytez-network <container>`) and set `image-server.url: http://image-server:8123` so the backend talks directly to the local container without leaving your machine.

#### Starting the Stack
To spin up both the backend API and PostgreSQL with automatic health-checking and container networking:

```bash
# In the backend directory
docker compose up --build -d
```

To stop containers:
```bash
docker compose down
```

---

## 🧪 Testing

The backend includes comprehensive Unit, Integration (using Testcontainers), and End-to-End (E2E) tests.

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Suites
- **Controller Unit Tests**:
  ```bash
  ./mvnw test -Dtest="*ControllerTest"
  ```
- **Integration Tests**:
  ```bash
  ./mvnw test -Dtest="*IntegrationTest"
  ```
- **End-to-End Tests**:
  ```bash
  ./mvnw test -Dtest="*E2ETest"
  ```
- **Single Test Class**:
  ```bash
  ./mvnw test -Dtest="PostServiceTest"
  ```
- **Single Test Method**:
  ```bash
  ./mvnw test -Dtest="PostServiceTest#getPostById_Found_ReturnsPostDto"
  ```

---

## 📡 API Endpoints Overview

| Area | Method | Endpoint | Access | Description |
| :---: | :---: | :--- | :---: | :--- |
| **Health** | `GET` | `/health` | Public | System status and service health check |
| **Auth** | `POST` | `/api/auth/register` | Public | Register new user account |
| **Auth** | `POST` | `/api/auth/login` | Public | Authenticate user & return JWT token |
| **Subreddits** | `GET` | `/api/subreddits` | Public | List all subreddits |
| **Subreddits** | `POST` | `/api/subreddits` | Authenticated | Create a new subreddit |
| **Posts** | `GET` | `/api/posts` | Public | Fetch feed posts (with pagination/sorting) |
| **Posts** | `GET` | `/api/posts/{id}` | Public | Get single post details |
| **Posts** | `POST` | `/api/posts` | Authenticated | Create post (text or image) |
| **Posts** | `POST` | `/api/posts/{id}/vote` | Authenticated | Upvote / Downvote a post |
| **Comments** | `GET` | `/api/posts/{id}/comments`| Public | List comments on a post |
| **Comments** | `POST` | `/api/posts/{id}/comments`| Authenticated | Add a comment to a post |
| **Filters** | `GET` | `/api/filters` | Public | Retrieve list of available image filters |

For complete JSON request payloads, response schemas, and interactive testing, refer to the [API Specification](https://zth-cog-fe.netlify.app/api-docs).

---

## 🐳 Containerization

The backend uses a multi-stage Docker build (`Dockerfile`):
1. **Stage 1 (Build)**: `maven:3.9-amazoncorretto-25` compiles and packages the JAR.
2. **Stage 2 (Runtime)**: `amazoncorretto:25-alpine` provides a lightweight Alpine-based Java runtime environment for running the compiled JAR.

## Production deployment on EC2

For the complete backend-only AWS setup, including EC2 bootstrap, IAM roles, GitHub OIDC, Amazon ECR, Systems Manager deployment, health verification, backups, and rollback, follow the [Backend-only EC2 deployment guide](EC2_DEPLOYMENT.md).
