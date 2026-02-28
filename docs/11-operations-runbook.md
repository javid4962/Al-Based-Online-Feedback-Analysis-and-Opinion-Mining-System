# 11 - Operations Runbook

## Daily Checks

- API health endpoint.
- Failed jobs in `job_run`.
- Model version activation status.

## Backup

- Daily MySQL dump.
- Daily model artifact backup.
- Keep minimum 14 days of backups.

## Retraining Operations

- Scheduled weekly retrain.
- Manual retrain endpoint for emergency model refresh.
- Validate metrics report before activation.

## Incident Response

1. Identify impact scope (API, DB, model, jobs).
2. Pause model activation changes.
3. Restore last stable model version if inference quality degrades.
4. Restore database from latest backup if data corruption occurs.

## Model Rollback Procedure

1. List model versions.
2. Select previous stable version.
3. Call activation endpoint.
4. Trigger short reanalysis smoke test.
