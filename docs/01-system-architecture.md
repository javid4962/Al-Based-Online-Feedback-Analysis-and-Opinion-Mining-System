# 01 - System Architecture

## High-Level Components

1. React SPA (`frontend`)
2. Spring Boot API (`backend`)
3. ML Core Library (`ml-core`)
4. MySQL Database
5. Filesystem Model Artifact Store

## Data Flow

1. User submits feedback via API or CSV upload.
2. Backend stores raw feedback in `feedback_item`.
3. Inference service uses active model from registry.
4. Prediction is stored in `sentiment_prediction`.
5. Aggregation jobs update `analytics_daily`.
6. Frontend fetches analytics and detailed feedback views.

## Backend Internal Boundaries

- `controller`: HTTP endpoints.
- `service`: business orchestration.
- `security`: JWT + filter + config.
- `repository`: data access.
- `entity`: JPA entities.
- `scheduler`: retraining and operational jobs.

## ML Boundary

`backend` never owns training internals directly. It calls `ml-core` interfaces for:

- preprocessing
- train/evaluate
- predict
- artifact save/load

## Non-Functional Focus

- Predictable native deployment (systemd + Nginx).
- Safe model activation workflow.
- Auditable operations (train, activate, login, job execution).
