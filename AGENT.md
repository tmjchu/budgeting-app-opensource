# Agent Instructions

## Project Overview

Local Budget is a local-first personal budgeting app. It has a Java 21 Spring Boot backend with local CSV persistence and Plaid integration, plus a React/Vite frontend dashboard.

## Repository Layout

- `backend/`: Spring Boot backend.
  - `src/main/java/com/localbudget/app/api`: REST controllers and API request/response models.
  - `src/main/java/com/localbudget/app/domain`: domain models, handlers, and services.
  - `src/main/java/com/localbudget/app/data`: CSV record models and repositories.
  - `src/main/java/com/localbudget/app/gateway`: external integrations, including Plaid.
  - `src/test/java/com/localbudget/app`: backend tests.
- `frontend/`: React 18 + TypeScript + Vite frontend.
  - `src/components`: dashboard components.
  - `src/hooks`: React hooks.
  - `src/lib`: API client, shared types, and formatting helpers.
- `docs/assets`: screenshots and documentation assets.

## Local Commands

Backend:

```bash
cd backend
mvn test
mvn spotless:check
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run build
npm run dev -- --host 127.0.0.1
```

The frontend runs at `http://127.0.0.1:5173` by default.

## Environment

Backend runtime expects Plaid configuration through environment variables:

```bash
PLAID_CLIENT_ID=your_client_id
PLAID_SECRET=your_secret
PLAID_ENV=sandbox
```

CSV data is written under `backend/data` by default. Override with:

```bash
BUDGET_DATA_DIRECTORY=/path/to/data
```

Do not commit local data files, real Plaid credentials, access tokens, or personal financial exports.

## Coding Guidelines

- Keep the backend layered: controllers translate HTTP, handlers orchestrate use cases, services own business logic, repositories handle CSV persistence, and gateways isolate external APIs.
- Prefer immutable domain values where practical and keep converters explicit between API, domain, CSV, and Plaid models.
- Preserve local-first behavior. Avoid adding hosted services, analytics, telemetry, or network dependencies unless the user explicitly asks.
- Keep frontend changes component-focused and typed. Put shared formatting/types/API calls in `frontend/src/lib`.
- Match the existing visual style: clear dashboard controls, dense-but-readable financial summaries, and no marketing-style landing page for app screens.
- Use focused tests for changed backend services, repositories, converters, and handlers. Add frontend build verification for TypeScript/UI changes.

## Verification Expectations

Run the narrowest useful checks after changes:

- Backend logic or API changes: `cd backend && mvn test`.
- Backend formatting-sensitive changes: `cd backend && mvn spotless:check`.
- Frontend TypeScript/UI changes: `cd frontend && npm run build`.

If a check cannot be run, explain why in the final response.
