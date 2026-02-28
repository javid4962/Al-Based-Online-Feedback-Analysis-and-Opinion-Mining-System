# 07 - Backend Implementation Steps

## Step 1: Bootstrap

- Create Spring Boot app entrypoint.
- Add package structure for controller/service/repository/entity/security.

## Step 2: Persistence

- Add Flyway V1 migration.
- Map entities and repositories.

## Step 3: Security

- Implement JWT service.
- Add stateless security filter chain.
- Provide single admin login endpoint.

## Step 4: Feedback Ingestion

- Implement single feedback create endpoint.
- Implement CSV upload endpoint with job tracking.

## Step 5: Inference and Prediction

- On feedback ingest, run inference using active model.
- Persist prediction and label provenance.

## Step 6: Analytics

- Overview metrics endpoint.
- Topic extraction endpoint.

## Step 7: Model Operations

- Trigger training run.
- List model versions.
- Activate model version.

## Step 8: Scheduling

- Weekly scheduled retraining.
- Reanalyze batch endpoint and async job handling.
