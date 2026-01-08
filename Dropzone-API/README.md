# 📦 Dropzone API

![Java CI](https://github.com/wesley00s/dropzone/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green)
![Terraform](https://img.shields.io/badge/Terraform-IaC-purple)
![Postgres](https://img.shields.io/badge/PostgreSQL-15-blue)

**Dropzone** is a robust API for file management and storage (Upload/Download), leveraging **AWS S3** for object storage and **PostgreSQL** for metadata.

The project adheres to modern development best practices, featuring **Infrastructure as Code (IaC)** with Terraform and reliable integration testing using **Testcontainers** and **LocalStack**.

---

## 🚀 Technologies

- **Java 21** (LTS)
- **Spring Boot 4.0.1**
- **Spring Data JPA** (Persistence)
- **Flyway** (Database Migration)
- **AWS SDK v2** (S3 Integration)
- **Terraform** (Infrastructure provisioning)
- **Docker & Docker Compose** (Containerization)
- **OpenAPI / Swagger** (API Documentation)

### 🧪 Testing & Quality
- **JUnit 5**
- **Testcontainers** (Ephemeral testing environment)
- **LocalStack** (Real AWS S3 simulation)
- **GitHub Actions** (CI Pipeline)

---

## ⚙️ Prerequisites

To run this project, you will need:

- **Java JDK 21**
- **Docker** (Essential for tests and local run)
- **Terraform** (Optional, if you plan to deploy infrastructure)

---

## 🛠️ How to Run

### 1. Clone the repository
```bash
git clone https://github.com/Wesley00s/dropzone.git
cd dropzone
```

### 2. Configure Environment & Infrastructure
The project includes example configuration files. Create your local copies:

```bash
# Application Environment Variables
cp .env.example .env

# Terraform Infrastructure Variables
cp infra/terraform.tfvars.example infra/terraform.tfvars
```

Ensure the variables inside `.env` match your local setup, and adjust `infra/terraform.tfvars` if you plan to deploy resources.

### 3. Run with Docker Compose (Recommended)
Since the project includes a `docker-compose.yml`, you can spin up the database and the application together:

```bash
docker-compose up -d
```

### 4. Run with Gradle (Manual)
If you prefer running the app directly on the host (requires a running Postgres instance):

```bash
./gradlew bootRun
```
The application will be available at `http://localhost:8080`.

---

## 🧪 Running Tests

This project uses **Testcontainers**. You do not need to manually configure a database or LocalStack to run the tests. Docker handles everything automatically.

```bash
./gradlew test
```

**What is covered?**
* **Integration:** `S3StorageProviderTest`, `FileControllerTest`, `FileServiceTest`, `FileMetadataRepositoryTest`.
* **Unit:** `S3StorageProviderUnitTest`, `GlobalExceptionHandlerTest`.
* **Infra:** `AbstractIntegrationTest` handles the Docker lifecycle.

---

## 📚 API Documentation

With the application running, access the interactive documentation:

```
http://localhost:8080/swagger-ui.html
```

---

## 🏗️ Project Structure

```text
.
├── docker-compose.yml      # Local development environment (App + DB)
├── infra/                  # Terraform IaC files (main.tf, variables)
├── src/
│   ├── main/
│   │   ├── java/com/dropzone/
│   │   │   ├── api/v1/     # Controllers (FileController) & DTOs
│   │   │   ├── core/       # Business Logic (FileService)
│   │   │   ├── domain/     # Entities (FileMetadata) & Repositories
│   │   │   └── infra/      # Config, S3 Providers & Exception Handling
│   │   └── resources/
│   │       └── db/migration/ # Flyway SQL Scripts (V1__Create_TB...)
│   └── test/
│       ├── java/com/dropzone/
│       │   ├── api/v1/     # Integration Tests for Controllers
│       │   ├── core/       # Integration Tests for Services
│       │   ├── infra/      # Base Testcontainers setup (AbstractIntegrationTest)
│       │   └── ...         # Unit & Repository Tests
│       └── resources/      # Test-specific properties
└── build.gradle            # Dependencies and Plugins
```

---

## 🤝 Contribution

1. Fork the project
2. Create a Branch (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add some AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License.