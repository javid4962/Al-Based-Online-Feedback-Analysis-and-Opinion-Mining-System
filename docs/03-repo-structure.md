# 03 - Repository Structure

## Top-Level Layout

- `backend/`: Spring Boot app.
- `ml-core/`: Java ML module.
- `frontend/`: React SPA.
- `docs/`: documentation-first implementation guides.
- `Documentation/`: source research paper and original planning artifacts.

## Backend Layout

- `config/`: app-level config classes.
- `controller/`: REST controllers.
- `dto/`: API request/response payloads.
- `entity/`: JPA entities.
- `enums/`: shared enum types.
- `repository/`: Spring Data JPA repositories.
- `security/`: JWT and auth filters.
- `service/`: service interfaces and implementations.
- `scheduler/`: scheduled jobs.
- `startup/`: initialization hooks.

## ML-Core Layout

- `config/`: model/training configuration records.
- `pipeline/`: preprocessing and tokenization.
- `model/`: HBGRU-LSTM builder/trainer wrapper.
- `service/`: prediction + training service contracts.
- `util/`: helper classes.

## Frontend Layout

- `pages/`: route-level pages.
- `components/`: reusable UI components.
- `api/`: HTTP client + API wrappers.
- `auth/`: token/session helpers.
- `types/`: TypeScript shared types.
