# Dropzone - FullStack File Manager

Dropzone is a complete file management solution that features drag & drop upload, paginated listing, secure downloading (via pre-signed URLs), and file deletion. The application is built upon a modern architecture using microservices principles, containerization, and Infrastructure as Code (IaC).

## 🚀 Tech Stack

### Backend (`Dropzone-API`)
- **Language:** Java 21
- **Framework:** Spring Boot 3+
- **Build Tool:** Gradle 8+
- **Database:** PostgreSQL
- **Storage:** AWS S3 (via LocalStack for development)
- **Testing:** JUnit 5, Mockito, Spring Boot Test

### Frontend (`Dropzone-UI`)
- **Framework:** Angular 17+ (Standalone Components)
- **Styling:** Tailwind CSS
- **Web Server:** Nginx (for production/docker)
- **Language:** TypeScript

### Infrastructure & DevOps
- **Containerization:** Docker & Docker Compose
- **IaC (Infra as Code):** Terraform
- **Cloud Mocking:** LocalStack (AWS local simulation)
- **CI/CD:** GitHub Actions

---

## 🏗️ Project Architecture

The project is organized as a monorepo containing both backend and frontend:

```text
Dropzone-FullStack/
├── docker-compose.yml       # Orchestration of all services (App, UI, DB, AWS Mock)
├── .env                     # Centralized environment variables
├── Dropzone-API/            # Spring Boot RESTful API
│   ├── src/                 # Java source code
│   └── infra/               # Terraform scripts to provision S3
└── Dropzone-UI/             # Angular SPA Application
    ├── src/                 # TypeScript/HTML/CSS source code
    └── nginx.conf           # Web server configuration
```

---

## 📋 Prerequisites

To run the full project, you only need:

- **Docker** and **Docker Compose** installed.

For local development (running without Docker containers):
- Java JDK 21
- Node.js 20+
- AWS CLI (optional, for LocalStack debugging)

---

## ⚡ How to Run (Docker Mode - Recommended)

This method spins up the entire stack: Database, LocalStack (S3), Backend API, Frontend, and automatically runs Terraform to create the S3 Bucket.

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Wesley00s/Dropzone-FullStack.git
   cd Dropzone-FullStack
   ```

2. **Configure environment variables:**
   Copy the example file. The default values work out-of-the-box for Docker.
   ```bash
   cp .env.example .env

   # Terraform Infrastructure Variables
   cp Dropzone-API/infra/terraform.tfvars.example Dropzone-API/infra/terraform.tfvars
   ```

3. **Start the containers:**
   ```bash
   docker-compose up -d --build
   ```

4. **Access the application:**
    - **Frontend (UI):** [http://localhost:4200](http://localhost:4200)
    - **Backend (API):** [http://localhost:8080/api/files](http://localhost:8080/api/files)
    - **LocalStack (AWS Mock):** [http://localhost:4566](http://localhost:4566)

---

## 🛠️ Local Development (Manual)

If you wish to run the applications outside of Docker for development purposes:

### 1. Start Base Infrastructure
You still need Postgres and LocalStack running.
```bash
docker-compose up -d db localstack terraform-init
```

### 2. Backend (Spring Boot)
Navigate to the API folder:
```bash
cd dropzone-api
```
Run the project:
```bash
./gradlew bootRun
```
*The API will be available at `http://localhost:8080`.*

### 3. Frontend (Angular)
Navigate to the UI folder:
```bash
cd ../Dropzone-UI
```
Install dependencies and run the dev server:
```bash
npm install
npm start
```
*Access at `http://localhost:4200`.*

---

## 🧪 Testing

### Backend
The project includes unit and integration tests covering Controllers, Services, and Storage Providers.

```bash
cd dropzone-api
./gradlew test
```

### Frontend
Unit tests using Jasmine/Karma.

```bash
cd Dropzone-UI
npm test
```

---

## ☁️ Infrastructure (Terraform & LocalStack)

S3 Bucket provisioning is automated via Terraform.

- **Local:** The `docker-compose.yml` file includes a `terraform-init` service that automatically applies the `main.tf` located in `dropzone-api/infra` when LocalStack starts.
- **Production:** The same `.tf` files can be used to provision real AWS infrastructure by simply changing credentials and endpoints in the variables file.

### Useful Terraform Commands (Manual)
If you want to apply infrastructure changes without restarting Docker:

```bash
cd dropzone-api/infra
terraform init
terraform apply -var="aws_endpoint=http://localhost:4566"
```

---

## 📦 Environment Variables (.env)

Main variables configured in `.env`:

| Variable | Description | Docker Default |
|----------|-------------|----------------|
| `POSTGRES_USER` | DB User | postgres |
| `POSTGRES_PASSWORD` | DB Password | postgres |
| `POSTGRES_DB` | DB Name | dropzone_db |
| `AWS_ACCESS_KEY_ID` | Mock AWS Key | test |
| `AWS_SECRET_ACCESS_KEY` | Mock AWS Secret | test |
| `AWS_REGION` | AWS Region | us-east-1 |
| `AWS_S3_BUCKET` | Bucket Name | dropzone-bucket |
| `AWS_ENDPOINT` | S3 Endpoint | http://localstack:4566 |

---

## 📸 UI Features

- **Drag and Drop:** Intuitive upload area supporting multiple files.
- **Upload Progress:** Visual feedback during file transmission.
- **Modern Dark Mode:** Neutral gray-scale interface for visual comfort.
- **Pagination:** Efficient navigation through large lists of files.
- **Dynamic Icons:** Icons generated based on file extension (PDF, Image, Audio, Code, etc.).

---

## 📄 License

This project is licensed under the MIT License. Feel free to contribute!
