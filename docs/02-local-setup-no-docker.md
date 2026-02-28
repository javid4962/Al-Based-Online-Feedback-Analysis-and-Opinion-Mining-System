# 02 - Local Setup (No Docker)

## Prerequisites

- Linux machine (Ubuntu 22.04+ recommended)
- Java 17+ (Java 21 recommended)
- Maven 3.9+
- Node.js 20+ and npm 10+
- MySQL 8.x

## MySQL Setup

1. Create database:
```sql
CREATE DATABASE feedback_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
2. Create user and grant privileges:
```sql
CREATE USER 'feedback_user'@'localhost' IDENTIFIED BY 'feedback_pass';
GRANT ALL PRIVILEGES ON feedback_ai.* TO 'feedback_user'@'localhost';
FLUSH PRIVILEGES;
```

## Backend Environment Variables

Set before running backend:

- `DB_URL=jdbc:mysql://localhost:3306/feedback_ai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- `DB_USERNAME=feedback_user`
- `DB_PASSWORD=feedback_pass`
- `JWT_SECRET=<base64-encoded-256-bit-secret>`
- `APP_ADMIN_USERNAME=admin`
- `APP_ADMIN_PASSWORD=ChangeMeNow123!`

Generate JWT secret example:
```bash
openssl rand -base64 32
```

## Run Backend

```bash
mvn -pl backend -am spring-boot:run
```

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

## Verify

- Backend health: `GET http://localhost:8080/actuator/health`
- Frontend app: `http://localhost:5173`
