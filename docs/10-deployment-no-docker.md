# 10 - Deployment (No Docker)

## Target

Single Linux VM deployment with native services.

## Components

- Spring Boot backend as systemd service.
- MySQL native service.
- Nginx reverse proxy.
- Frontend static build served by Nginx.

## Backend Service Example

`/etc/systemd/system/feedback-api.service`:

```ini
[Unit]
Description=Feedback API
After=network.target mysql.service

[Service]
User=feedback
WorkingDirectory=/opt/feedback-api
Environment=DB_URL=jdbc:mysql://localhost:3306/feedback_ai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
Environment=DB_USERNAME=feedback_user
Environment=DB_PASSWORD=feedback_pass
Environment=JWT_SECRET=<base64-secret>
Environment=APP_ADMIN_USERNAME=admin
Environment=APP_ADMIN_PASSWORD=<strong-password>
ExecStart=/usr/bin/java -jar /opt/feedback-api/backend.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

## Nginx Reverse Proxy

- `/api/` -> backend `localhost:8080`
- `/` -> frontend static files

## Rollout and Rollback

- Keep previous backend JAR and previous active model artifacts.
- Rollback by switching systemd symlink to prior JAR and re-activating prior model version.
