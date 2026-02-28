# 04 - Database Schema

## Tables

1. `admin_user`
- Stores single-admin credentials and status.

2. `feedback_item`
- Raw input feedback payloads and metadata.

3. `feedback_label`
- Label provenance (`AUTO_RULE` or `MODEL`) for retraining traceability.

4. `sentiment_prediction`
- Model inference outputs per feedback entry.

5. `model_version`
- Model artifact registry, activation state, config snapshot.

6. `training_run`
- Training run metadata and metrics JSON.

7. `job_run`
- Async job tracking (CSV ingest, reanalyze, train).

8. `analytics_daily`
- Pre-aggregated daily metrics by source.

9. `audit_log`
- Security and operations audit trail.

## Index Strategy

- `feedback_item(submitted_at)` for timeline filtering.
- `sentiment_prediction(feedback_id, inferred_at)` for latest prediction lookup.
- `model_version(is_active)` for active model fetch.
- `job_run(status, started_at)` for job dashboard queries.
- `analytics_daily(date_key, source_type)` for dashboard range scans.

## Migration Rules

- Use Flyway SQL migrations under `backend/src/main/resources/db/migration`.
- Never edit executed migration scripts.
- For changes, create next incremental migration.
