# 09 - Testing Strategy

## Unit Tests

- JWT generation/validation.
- Preprocessing behavior with negation retention.
- Label mapping from ratings.
- Service-level orchestration logic.

## Integration Tests

- Login flow.
- Feedback ingest -> prediction persistence.
- CSV upload job creation and completion.
- Model activation and active model fetch.

## API Contract Tests

- Required fields and validation errors.
- Auth-required endpoint checks.

## ML Validation

- Holdout metrics report generation.
- Negative-class F1 gate validation.
- Candidate vs active model regression checks.

## Performance Tests

- Inference p95 latency.
- Batch reanalyze throughput.
- Concurrent feedback ingest reliability.

## Security Tests

- Unauthorized access rejection.
- Input sanitization for CSV and text payloads.
- Audit record generation for critical actions.
