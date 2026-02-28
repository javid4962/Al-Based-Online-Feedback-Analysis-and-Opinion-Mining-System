# 00 - Project Charter

## Goal

Build a production MVP for AI-based online feedback analysis and opinion mining using full-stack Java technologies with no Docker dependency.

## Scope

- Ingest feedback using REST API and CSV upload.
- Predict sentiment (positive/negative) with confidence score.
- Provide analytics endpoints and dashboard views.
- Support training runs, model versioning, and model activation.
- Support realtime inference and batch reanalysis.

## Out of Scope (MVP)

- Multi-tenant architecture.
- Multilingual model support.
- Social platform connector integrations (X/Reddit/etc.).
- External SSO integrations.

## KPI Targets

- Primary ML KPI: negative-class `F1 >= 0.93`.
- API availability for core paths (`/auth`, `/feedback`, `/predictions`, `/analytics`) during pilot.
- Inference latency target: `p95 < 40ms` per review (production-grade hardware).

## Constraints

- Must use native Linux deployment, no Docker.
- Java-first stack for backend and ML lifecycle.
- MySQL as relational persistence.

## Assumptions

- English-only sentiment analysis in MVP.
- Single admin role in MVP.
- Weekly scheduled retraining plus manual trigger.
