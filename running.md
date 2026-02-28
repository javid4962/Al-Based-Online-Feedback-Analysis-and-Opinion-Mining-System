# Running the Application

This guide explains how to run the full system locally (backend + frontend) without Docker.

## Quick Start Scripts by OS

From project root:

```bash
# macOS
./run_macos.sh

# Windows (Git Bash or WSL)
./run_windows.sh
```

Both scripts delegate to `run_from_scratch.sh` and only set OS-friendly defaults:
- macOS: `MYSQL_CONNECTION_MODE=socket`
- Windows: `MYSQL_CONNECTION_MODE=tcp`

You can still override any variable before running, for example:

```bash
export DB_PASSWORD="your_password"
./run_windows.sh
```

## 1. Prerequisites

- Java 17+ (Java 21 recommended)
- Maven 3.9+
- Node.js 20+ and npm 10+
- MySQL 8.x

## 2. Start MySQL and Create Database

Open MySQL and run:

```sql
CREATE DATABASE feedback_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'feedback_user'@'localhost' IDENTIFIED BY 'feedback_pass';
GRANT ALL PRIVILEGES ON feedback_ai.* TO 'feedback_user'@'localhost';
FLUSH PRIVILEGES;
```

## 3. Configure Environment Variables (Backend)

In your terminal (from project root), set:

```bash
export DB_URL="jdbc:mysql://localhost:3306/feedback_ai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="feedback_user"
export DB_PASSWORD="feedback_pass"
export JWT_SECRET="$(openssl rand -base64 32)"
export APP_ADMIN_USERNAME="admin"
export APP_ADMIN_PASSWORD="ChangeMeNow123!"
```

Optional:

```bash
export MODEL_ARTIFACTS_DIR="./artifacts"
export CSV_MAX_BYTES="5242880"
```

## 4. Run Backend (Spring Boot API)

From project root:

```bash
mvn -pl backend -am spring-boot:run
```

Backend will start on: `http://localhost:8080`

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## 5. Run Frontend (React)

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend will start on: `http://localhost:5173`

If needed, point frontend to backend explicitly:

```bash
export VITE_API_BASE="http://localhost:8080"
npm run dev
```

## 6. First Login

Use:

- Username: `admin`
- Password: `ChangeMeNow123!` (or your `APP_ADMIN_PASSWORD`)

The admin user is auto-created on backend startup if it does not exist.

## 7. Quick API Smoke Test

Get token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"ChangeMeNow123!"}'
```

Then call a protected endpoint with `Authorization: Bearer <token>`.

## 8. Common Issues

- `Access denied for user`: verify `DB_USERNAME`/`DB_PASSWORD` and MySQL grants.
- `JWT secret too short/invalid`: regenerate with `openssl rand -base64 32`.
- `Frontend cannot reach API`: set `VITE_API_BASE=http://localhost:8080`.
- Port conflicts: change ports in `backend/src/main/resources/application.yml` or `frontend/vite.config.ts`.
