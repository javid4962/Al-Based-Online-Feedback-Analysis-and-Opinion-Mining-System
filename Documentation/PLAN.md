# AI-Based Online Feedback Analysis and Opinion Mining System (Full-Stack Java, No Docker)

## Summary
Build a production MVP that operationalizes the paper’s HBGRU-LSTM approach in a full-stack Java system with Spring Boot, MySQL, DL4J, and React.  
The system will ingest online feedback via REST and CSV, run real-time + batch sentiment inference, provide analytics dashboards, and support scheduled/manual model retraining.  
Primary quality gate: `F1-score (negative class) >= 0.93` on the holdout test set.

## Paper-Derived Design Decisions (Locked)
1. Model architecture: `Embedding(128) -> Bidirectional GRU(256) -> LSTM(128) -> Dense(1, sigmoid)`.
2. Regularization: dropout `0.20` in recurrent stack.
3. Training defaults: Adam `lr=0.001`, batch size `128`, max epochs `100`, early stopping enabled.
4. Dataset strategy: combine IMDB + Amazon style corpora; enforce near-balanced class distribution for training.
5. Metrics: accuracy, precision, recall, F1, confusion matrix; prioritize minority (negative) recall/F1.
6. Runtime objective: low-latency inference suitable for operational feedback streams.

## Target Architecture
1. `feedback-platform-api` (Spring Boot 3.x, Java 21):
- REST APIs for ingest, prediction, analytics, model ops, auth.
- Asynchronous job orchestration for inference and retraining.
- Reads/writes MySQL and model artifact storage.
2. `feedback-ml-core` (Java library inside same repo):
- Text preprocessing, tokenization, vectorization.
- HBGRU-LSTM training/evaluation/inference with DL4J.
- Model serialization/deserialization and version metadata.
3. `feedback-platform-ui` (React + TypeScript):
- Admin login, ingest screens, analytics dashboards, model management.
4. Persistence:
- MySQL 8 for operational and analytics data.
- Filesystem artifact store for model binaries and training reports.
5. Deployment (no Docker):
- Spring Boot executable JAR(s) via `systemd`.
- MySQL native install.
- Nginx reverse proxy serving React static assets + API routing.

## Public APIs / Interfaces / Types

### REST API (v1)
1. `POST /api/v1/auth/login`
- Request: `LoginRequest { username, password }`
- Response: `AuthTokenResponse { accessToken, expiresAt }`
2. `POST /api/v1/feedback`
- Request: `FeedbackCreateRequest { sourceType, sourceRef, text, rating?, locale, submittedAt }`
- Response: `FeedbackResponse { feedbackId, status }`
3. `POST /api/v1/feedback/bulk-csv`
- Multipart CSV; async ingestion job.
- Response: `JobResponse { jobId, status }`
4. `GET /api/v1/feedback`
- Query filters: source, date range, sentiment, confidence, keyword.
- Response: paginated `FeedbackListResponse`.
5. `GET /api/v1/predictions/{feedbackId}`
- Response: `PredictionResponse { label, confidence, modelVersion, inferredAt }`
6. `POST /api/v1/predictions/reanalyze`
- Request: `ReanalyzeRequest { fromDate, toDate, sourceType? }`
- Response: `JobResponse`.
7. `GET /api/v1/analytics/overview`
- Response: `OverviewMetrics { total, positivePct, negativePct, trendSeries }`
8. `GET /api/v1/analytics/topics`
- Response: key phrases per sentiment bucket.
9. `POST /api/v1/models/train`
- Request: `TrainRequest { datasetPolicy, epochs?, batchSize?, learningRate?, rebalance=true }`
- Response: `TrainingRunResponse { runId, status }`
10. `GET /api/v1/models`
- Response: `ModelVersionListResponse`.
11. `POST /api/v1/models/{versionId}/activate`
- Response: `ModelActivationResponse { activeVersionId }`
12. `GET /api/v1/jobs/{jobId}`
- Response: `JobStatusResponse`.

### Core Java Interfaces
1. `FeedbackIngestionService`
- `ingestSingle(FeedbackCreateRequest)`
- `ingestCsv(InputStream csv)`
2. `PreprocessingPipeline`
- `normalize(String text)`
- `tokenize(String text)`
- `vectorize(List<String> tokens)`
3. `SentimentModelService`
- `train(TrainingConfig cfg)`
- `predict(String text, String activeModelVersion)`
- `evaluate(ModelVersion version, DatasetRef testSet)`
4. `ModelRegistryService`
- `register(ModelArtifact, MetricsReport)`
- `activate(String versionId)`
- `getActive()`
5. `AnalyticsService`
- `getOverview(FilterCriteria)`
- `getTrends(FilterCriteria)`
- `getTopicSummary(FilterCriteria)`

### Database Schema (MySQL)
1. `admin_user`
- `id, username, password_hash, is_active, created_at, last_login_at`
2. `feedback_item`
- `id, source_type, source_ref, text_raw, rating, locale, submitted_at, ingested_at`
3. `feedback_label`
- `feedback_id, label_source(AUTO_RULE|MODEL), sentiment_label, confidence, labeled_at`
4. `sentiment_prediction`
- `id, feedback_id, model_version_id, sentiment_label, confidence, logits_json, inferred_at`
5. `model_version`
- `id, version_name, artifact_path, vocab_path, config_json, is_active, created_at`
6. `training_run`
- `id, started_at, ended_at, status, train_size, test_size, class_balance_json, metrics_json, notes`
7. `job_run`
- `id, job_type(INGEST|REANALYZE|TRAIN), status, payload_json, progress, started_at, ended_at`
8. `analytics_daily`
- `date_key, source_type, total_count, pos_count, neg_count, avg_confidence`
9. `audit_log`
- `id, actor, action, target_type, target_id, payload_json, created_at`

## End-to-End Data Flow
1. Feedback enters through REST or CSV.
2. Ingestion persists raw feedback and queues inference job.
3. Active model predicts sentiment + confidence; prediction stored in `sentiment_prediction`.
4. Aggregation job updates `analytics_daily` for fast dashboard queries.
5. Weekly scheduled retraining builds training dataset from:
- Seed benchmark data.
- Production records with `rating`-based auto-label rules (`1-2 => negative`, `4-5 => positive`, ignore `3/null`).
6. Retrain + evaluate; register new model version.
7. Admin can activate selected version.

## ML Pipeline Specification (DL4J)
1. Preprocessing:
- lowercase, strip HTML/URLs/special chars, preserve negation/boosters (`not`, `never`, `very`, `extremely`), remove non-informative stopwords.
2. Vocabulary:
- cap vocab to `50,000`.
- OOV token required.
3. Sequence policy:
- max tokens per review: `300`.
- pad/truncate to fixed length.
4. Split:
- stratified `70% train / 15% validation / 15% test`.
5. Rebalancing:
- enforce class ratio target `50:50` using controlled resampling at train-set level.
6. Model config:
- Embedding dim `128`.
- Bidirectional GRU hidden `256`.
- LSTM hidden `128`.
- Dense output `1` sigmoid.
- Dropout `0.20`.
- BCE loss.
- Adam `0.001`.
- Batch `128`.
- Max epochs `100`.
- Early stopping patience `8`.
7. Model governance:
- every trained artifact versioned and immutable.
- activation is metadata switch, never artifact overwrite.

## UI Scope (React SPA)
1. Login page (single-admin auth).
2. Dashboard:
- sentiment distribution, day/week trends, source breakdown, confidence distribution.
3. Feedback Explorer:
- searchable table, filters, detail drawer with prediction metadata.
4. Ingestion:
- CSV upload status and API ingest test form.
5. Model Center:
- training runs, metric comparison, activate model action.
6. Jobs Monitor:
- running/completed async tasks with progress and logs.

## Implementation Phases
1. Foundation:
- monorepo structure, Maven modules, Spring Boot skeleton, MySQL migrations, React scaffold.
2. Core data + auth:
- schema creation, single-admin JWT auth, audit logging.
3. Ingestion + prediction:
- REST/CSV ingestion, async inference, persistence, error handling.
4. HBGRU-LSTM engine:
- preprocessing pipeline, DL4J model train/infer, artifact registry.
5. Analytics layer:
- aggregate jobs, overview/trend/topic APIs.
6. Frontend:
- dashboard, explorer, model center, jobs monitor.
7. Retraining automation:
- scheduler + manual trigger, evaluation reports, model activation workflow.
8. Hardening + release:
- performance tuning, security checks, operational docs, no-Docker deployment scripts.

## Testing and Acceptance Scenarios

### Backend/ML Tests
1. Unit tests:
- preprocessing correctness (negation preserved, token rules).
- label mapping from ratings.
- DTO validation and API contracts.
2. Integration tests:
- ingest -> predict -> persist full flow.
- CSV ingestion with malformed rows handling.
- model activation and rollback path.
3. ML validation tests:
- holdout metrics report includes accuracy/precision/recall/F1/confusion matrix.
- negative-class F1 gate fails build if `< 0.93`.
4. Regression tests:
- compare candidate model vs current active; block activation if materially worse on negative recall/F1.

### Performance/Operational Tests
1. Inference latency:
- target `p95 < 40ms` per single review on deployment hardware.
2. Batch prediction:
- `1000 reviews <= 2s` target envelope.
3. API load:
- ingest endpoint stable under agreed concurrent load with no data loss.
4. Recovery:
- service restart reloads active model and resumes queued jobs safely.

### Security Tests
1. Auth required on all admin endpoints.
2. CSV upload validation and size limits.
3. SQL injection/XSS checks.
4. Audit trail generated for model/train/activate/admin actions.

## Assumptions and Defaults
1. Stack: Java 21, Spring Boot 3.x, MySQL 8, React + TypeScript, DL4J.
2. Language scope: English-only MVP.
3. Feedback sources in MVP: REST ingestion + CSV bulk import.
4. Access model: single admin user in MVP.
5. Inference mode: real-time on ingest + batch re-analysis jobs.
6. Retraining: scheduled weekly plus manual trigger.
7. Labeling for retraining: auto-label only from rating metadata; unlabeled/neutral samples excluded.
8. Deployment constraint applied: no Docker; native Linux services only.
