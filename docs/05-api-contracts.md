# 05 - API Contracts

## Auth

### POST `/api/v1/auth/login`
Request:
```json
{
  "username": "admin",
  "password": "ChangeMeNow123!"
}
```
Response:
```json
{
  "accessToken": "<jwt>",
  "expiresAt": "2026-01-01T10:00:00Z"
}
```

## Feedback

### POST `/api/v1/feedback`
Request:
```json
{
  "sourceType": "REST",
  "sourceRef": "ticket-112",
  "text": "Delivery was late and support did not help",
  "rating": 2,
  "locale": "en",
  "submittedAt": "2026-02-27T10:00:00Z"
}
```
Response:
```json
{
  "feedbackId": 101,
  "status": "ACCEPTED"
}
```

### POST `/api/v1/feedback/bulk-csv`
Multipart upload (`file`).
Response:
```json
{
  "jobId": 44,
  "status": "RUNNING"
}
```

### GET `/api/v1/feedback`
Query params: `sourceType`, `sentiment`, `from`, `to`, `page`, `size`.

## Predictions

### GET `/api/v1/predictions/{feedbackId}`
Response:
```json
{
  "feedbackId": 101,
  "label": "NEGATIVE",
  "confidence": 0.94,
  "modelVersion": "hbgru-lstm-v1",
  "inferredAt": "2026-02-27T10:01:00Z"
}
```

### POST `/api/v1/predictions/reanalyze`
Request:
```json
{
  "fromDate": "2026-02-01",
  "toDate": "2026-02-27",
  "sourceType": "CSV"
}
```

## Analytics

### GET `/api/v1/analytics/overview`
Response includes totals and sentiment percentages.

### GET `/api/v1/analytics/topics`
Returns frequent tokens grouped by sentiment bucket.

## Models

### POST `/api/v1/models/train`
Request:
```json
{
  "datasetPolicy": "AUTO_LABEL_ONLY",
  "epochs": 100,
  "batchSize": 128,
  "learningRate": 0.001,
  "rebalance": true
}
```

### GET `/api/v1/models`
List versions and activation state.

### POST `/api/v1/models/{versionId}/activate`
Switch active model pointer.

## Jobs

### GET `/api/v1/jobs/{jobId}`
Returns status, progress, timestamps, summary payload.

## Error Model

```json
{
  "timestamp": "2026-02-27T10:02:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "text must not be blank",
  "path": "/api/v1/feedback"
}
```
