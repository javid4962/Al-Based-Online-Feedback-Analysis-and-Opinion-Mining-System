# AI-Based Online Feedback Analysis and Opinion Mining System

A documentation-first, full-stack Java MVP for online feedback sentiment analysis based on the HBGRU-LSTM approach from the base paper in `Documentation/technologies-13-00175-v2.pdf`.

## What This Project Delivers

- Spring Boot backend with MySQL + Flyway.
- Java-native ML module (`DL4J`) for sentiment model lifecycle.
- React frontend for admin workflows and analytics.
- REST + CSV ingestion.
- Realtime sentiment inference + batch reanalysis.
- Model registry, activation, training runs, and job tracking.
- No Docker deployment target (systemd + Nginx + MySQL native install).

## Architecture Snapshot

- `backend/`: APIs, security, persistence, scheduling, orchestration.
- `ml-core/`: preprocessing and HBGRU-LSTM model scaffolding.
- `frontend/`: React SPA for login, dashboard, feedback explorer, ingestion, model center, jobs monitor.
- `docs/`: step-by-step implementation and operational guides.

## Quickstart

1. Read setup guide: `docs/02-local-setup-no-docker.md`
2. Review architecture: `docs/01-system-architecture.md`
3. Start backend and MySQL.
4. Start frontend.
5. Test endpoints from `docs/05-api-contracts.md`.

## Documentation Roadmap

- Project charter: `docs/00-project-charter.md`
- System architecture: `docs/01-system-architecture.md`
- Local setup: `docs/02-local-setup-no-docker.md`
- Repo structure: `docs/03-repo-structure.md`
- Database schema: `docs/04-database-schema.md`
- API contracts: `docs/05-api-contracts.md`
- ML pipeline: `docs/06-ml-pipeline-hbgru-lstm.md`
- Backend steps: `docs/07-backend-implementation-steps.md`
- Frontend steps: `docs/08-frontend-implementation-steps.md`
- Testing strategy: `docs/09-testing-strategy.md`
- Deployment: `docs/10-deployment-no-docker.md`
- Operations runbook: `docs/11-operations-runbook.md`
- Milestones: `docs/12-milestones-checklist.md`

## Current Status

This repository now contains the end-to-end scaffold plus docs to implement and operate the system in phased execution.
