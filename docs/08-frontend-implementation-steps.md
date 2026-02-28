# 08 - Frontend Implementation Steps

## Page Set

1. Login
2. Dashboard
3. Feedback Explorer
4. Ingestion
5. Model Center
6. Jobs Monitor

## Route Plan

- `/login`
- `/dashboard`
- `/feedback`
- `/ingestion`
- `/models`
- `/jobs`

## API Integration

- Central HTTP client with JWT interceptor.
- Standardized error parser.
- Typed request/response contracts.

## State Management

- Local component state for MVP.
- Route guards using token presence and expiry.

## UX Requirements

- Clear job progress and model activation visibility.
- Filter controls for feedback and analytics date ranges.
