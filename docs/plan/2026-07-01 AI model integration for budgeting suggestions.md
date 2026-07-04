# AI Model Integration For Budgeting Suggestions

Date: 2026-07-01
Status: Draft

## Goal

Add an opt-in AI integration that lets users connect the budgeting app to an AI model/provider of their choice and request budgeting insights from their local banking transaction data.

The feature should act as a provider-neutral AI harness: the app compiles a controlled financial data summary, combines it with static prompt templates, sends it to the selected AI provider, and presents the returned budgeting suggestions to the user.

The feature must make it clear that the user is choosing to share banking history and derived financial summaries with the AI provider they configure.

## Context

The current application is local-first:

- Backend: Java 21 Spring Boot in `backend/`.
- Persistence: local CSV repositories under `backend/src/main/java/com/localbudget/app/data`.
- Domain logic: services and handlers under `backend/src/main/java/com/localbudget/app/domain`.
- Plaid integration: isolated behind gateway interfaces in `backend/src/main/java/com/localbudget/app/gateway`.
- Frontend: React 18 + TypeScript + Vite in `frontend/`.

This feature changes the privacy posture of the app. Today the app syncs financial data from Plaid and keeps local CSV data. With AI enabled, the app may send transaction history, balances, merchant names, categories, dates, account metadata, and derived summaries to an external AI provider selected by the user.

The design should follow the same architectural style as Plaid:

- Put external AI calls behind gateway interfaces.
- Keep orchestration in handlers.
- Keep business rules and prompt/data shaping in services.
- Keep API DTOs separate from domain models.
- Keep provider credentials out of source control.

Responsible AI references to consider during execution:

- NIST AI Risk Management Framework: https://www.nist.gov/itl/ai-risk-management-framework
- NIST Generative AI Profile, linked from the NIST AI RMF page, for generative-AI-specific risk management.

This plan is not legal advice. Before shipping broadly, the disclosure, financial-advice wording, and provider data-sharing behavior should be reviewed for the target jurisdictions.

## Scope

In scope:

- User opt-in flow for AI insights.
- AI provider/model configuration.
- Disclosure and consent before sending any financial data to an AI provider.
- Static prompt templates stored in the backend.
- Data compiler that prepares a bounded transaction summary for AI use.
- Provider-neutral AI gateway interface.
- Initial provider adapters for:
  - OpenAI-compatible HTTP APIs, useful for OpenAI, OpenRouter, local proxy servers, and similar providers.
  - Local model endpoints such as Ollama if the user wants local inference.
- AI insight run endpoint.
- Frontend AI settings and insights panel.
- Audit metadata for each AI run.
- Tests for consent checks, data compilation, prompt assembly, and gateway behavior.

Out of scope for the first version:

- Autonomous money movement.
- Investment, tax, credit, lending, insurance, or legal advice.
- Fine-tuning models on the user's data.
- Long-term vector storage of full transaction history.
- Sharing data with any provider before explicit user opt-in.
- Automatically sending every new transaction to AI.
- Server-hosted multi-user secret management.

## Proposed Changes

### Product Behavior

Add an "AI Insights" area to the app with three clear states:

1. Not configured:
   - Explain that AI insights require choosing an AI provider/model.
   - Show a privacy-first setup action.

2. Disclosure and consent:
   - Require an explicit checkbox or confirmation before enabling AI.
   - Make the user acknowledge that selected banking history and financial summaries may be sent to the configured AI provider.
   - Link the disclosure near the provider/model configuration and again before the first insight run.

3. Insights:
   - Let the user choose an analysis type, such as monthly spending review, category cleanup, subscription detection, savings opportunities, or budget suggestions.
   - Show what data range will be sent.
   - Allow running the analysis manually.
   - Display generated suggestions with "not financial advice" framing and a timestamp/provider/model label.

### Suggested Disclosure Copy

Use clear wording like:

```text
AI insights are optional. If you enable this feature, Local Budget will send selected banking history and financial summaries from this app to the AI provider and model you configure. This may include transaction dates, amounts, merchant names, categories, account names or types, balances, and spending summaries.

Your AI provider may process, store, or use this data according to its own terms and privacy policy. Review your provider's policies before continuing.

AI-generated suggestions can be incomplete or wrong. They are budgeting suggestions only and are not financial, investment, tax, legal, credit, or insurance advice. You are responsible for decisions you make from these suggestions.
```

Implementation notes:

- Require affirmative consent before the first run.
- Store consent timestamp, provider key/name, model id, and disclosure version.
- If disclosure text changes materially, require renewed consent.
- Provide a disable button and clear what disabling does and does not delete.

### AI Harness Architecture

Create backend abstractions similar to the Plaid gateway pattern:

- `AiGateway`
  - Interface for sending a prompt request to a configured model.
  - Returns raw provider metadata and normalized response content.

- `AiProviderConfig`
  - Provider type, base URL, model id, timeout, and credential reference.
  - Avoid logging secrets.

- `AiInsightType`
  - Enum or value object for supported prompt types.
  - Examples: `MONTHLY_REVIEW`, `CATEGORY_REVIEW`, `SUBSCRIPTION_REVIEW`, `BUDGET_SUGGESTIONS`, `SPENDING_ANOMALIES`.

- `AiPromptTemplateService`
  - Owns static prompts.
  - Assembles system and user messages from prompt templates plus compiled data.
  - Keeps prompts versioned.

- `AiFinancialContextService`
  - Compiles transaction, category, account, balance, and stats data.
  - Minimizes data sent to the model.
  - Supports date range and account filters.

- `AiInsightService`
  - Enforces consent.
  - Calls context compiler.
  - Calls prompt template service.
  - Calls gateway.
  - Parses/normalizes the response into displayable insight sections.

- `AiInsightRun`
  - Captures run metadata: timestamp, provider, model, prompt version, date range, transaction count, consent version, and status.
  - Do not store full prompt/response by default unless the user explicitly enables local history.

### Provider Strategy

Start with provider-neutral configuration rather than hardcoding one vendor.

Recommended first provider path:

- OpenAI-compatible chat completions adapter:
  - `baseUrl`
  - `apiKey`
  - `model`
  - request timeout

This supports a wide range of hosted and self-hosted routing services if they expose an OpenAI-compatible API.

Recommended second provider path:

- Local HTTP model adapter:
  - Ollama-style local endpoint.
  - No cloud sharing if the endpoint is local, but still disclose that data is sent to the configured endpoint.

Avoid adding a long provider list in the first implementation. Instead, support:

- Provider name
- Base URL
- Model id
- API key
- Compatibility mode

### Prompt Design

Prompts should be static, versioned, and scoped to budgeting suggestions.

Common system prompt principles:

- The model is a budgeting assistant.
- It must avoid investment, tax, legal, insurance, credit, or debt-settlement advice.
- It should cite the supplied data patterns in plain language.
- It should identify uncertainty and missing data.
- It should produce actionable but modest suggestions.
- It should never claim guaranteed savings or outcomes.

Data packet shape should be structured JSON, not prose, for predictable model input.

Example data sections:

- analysis period
- account summaries
- monthly totals
- category totals
- top merchants
- recurring merchant candidates
- largest transactions
- income vs spending
- balance snapshot summary
- excluded fields and redactions

For privacy, start with summaries and merchant/category rollups. Only include transaction-level details when necessary for a specific insight type.

### Response Shape

Ask the model to return JSON that the backend can validate before displaying:

```json
{
  "summary": "string",
  "suggestions": [
    {
      "title": "string",
      "rationale": "string",
      "estimatedMonthlyImpact": "string",
      "confidence": "low|medium|high",
      "relatedCategories": ["string"],
      "caveats": ["string"]
    }
  ],
  "questionsForUser": ["string"],
  "disclaimer": "string"
}
```

Backend should handle invalid JSON gracefully by returning a safe error or a plain-text fallback.

## Files And Areas To Inspect

Backend:

- `backend/src/main/java/com/localbudget/app/domain/service/TransactionQueryService.java`
- `backend/src/main/java/com/localbudget/app/domain/service/StatsAggregationService.java`
- `backend/src/main/java/com/localbudget/app/domain/service/BalanceSnapshotService.java`
- `backend/src/main/java/com/localbudget/app/domain/handler/GetTransactionsHandler.java`
- `backend/src/main/java/com/localbudget/app/domain/handler/GetMonthlyStatsHandler.java`
- `backend/src/main/java/com/localbudget/app/domain/handler/GetCategoryStatsHandler.java`
- `backend/src/main/java/com/localbudget/app/api/controller`
- `backend/src/main/java/com/localbudget/app/config/BudgetAppProperties.java`
- `backend/src/main/resources/application.yml`
- `backend/src/test/java/com/localbudget/app`

Frontend:

- `frontend/src/App.tsx`
- `frontend/src/lib/api.ts`
- `frontend/src/lib/types.ts`
- `frontend/src/components`
- `frontend/src/styles.css`

Documentation:

- `README.md`
- `AGENT.md`
- `CLAUDE.md`

## Execution Steps

1. Define product boundaries.
   - Rename "financial decisions" to "budgeting suggestions" throughout UI and API.
   - Decide first supported insight types.
   - Decide whether AI run history is stored locally or generated on demand only.

2. Add backend domain models.
   - Add AI provider config model.
   - Add AI consent model with disclosure version and timestamp.
   - Add AI insight request/result models.
   - Add AI insight type enum.

3. Add local persistence for AI settings.
   - Store provider settings in a local CSV or YAML-backed repository consistent with the current local-first pattern.
   - Never store API keys in committed files.
   - Prefer environment variables for API keys in the first version.
   - If local API key storage is added later, document that it is stored locally and should be protected by the user's machine security.

4. Add consent enforcement.
   - AI run handler must fail closed if consent is missing, stale, or for a different disclosure version.
   - AI setup endpoint records explicit consent.
   - Include provider/model and disclosure version in consent metadata.

5. Build the financial context compiler.
   - Reuse existing transaction, stats, category, account, and balance services.
   - Support date range and optional account filters.
   - Default to summarized data.
   - Include transaction-level data only for insight types that require it.
   - Add redaction or minimization options for merchant names if useful.

6. Build prompt template support.
   - Store prompt templates as backend resources or strongly typed constants.
   - Version each prompt.
   - Add prompt assembly tests.
   - Keep prompts static for the first version.

7. Add AI gateway interfaces and adapters.
   - Create `AiGateway` interface.
   - Create OpenAI-compatible adapter.
   - Optionally create local model adapter.
   - Add request timeout, error mapping, and secret-safe logging.
   - Avoid provider SDKs initially if a simple HTTP adapter is enough.

8. Add backend API endpoints.
   - `GET /api/ai/config`
   - `PUT /api/ai/config`
   - `POST /api/ai/consent`
   - `GET /api/ai/consent`
   - `POST /api/ai/insights`
   - Optional: `GET /api/ai/runs`

9. Add frontend settings UI.
   - Add an AI settings panel.
   - Let user choose provider compatibility mode, base URL, model, and key source.
   - Show disclosure before enabling.
   - Make disabled/unconfigured state clear.

10. Add frontend insights UI.
    - Add an AI Insights panel or tab.
    - Let user select insight type and date range.
    - Show a "data that will be shared" preview summary.
    - Display provider/model/run timestamp with results.
    - Display caveats and disclaimer near generated output.

11. Add tests.
    - Consent-required tests.
    - Data minimization tests for each insight type.
    - Prompt assembly tests.
    - Gateway request mapping tests with mock HTTP server.
    - Response parsing/error handling tests.
    - Frontend build verification.

12. Update documentation.
    - README: explain optional AI feature, provider setup, data-sharing warning, and local-first caveats.
    - AGENT.md: note AI privacy/security expectations for future agents.

## Verification

Backend:

```bash
cd backend
mvn test
mvn spotless:check
```

Frontend:

```bash
cd frontend
npm run build
```

Manual verification:

- Start backend and frontend.
- Confirm AI Insights is unavailable until configured.
- Confirm consent is required before first AI run.
- Confirm disclosure is visible before enabling AI.
- Confirm a missing/invalid API key gives a safe error.
- Confirm provider/model metadata is shown with generated results.
- Confirm disabling AI prevents further AI calls.
- Confirm no secrets appear in logs or browser-visible errors.

## Risks And Questions

Risks:

- Banking transaction data is highly sensitive. The feature must default to no AI sharing.
- AI output can be wrong or overconfident. UI and prompts should frame output as suggestions.
- Hosted AI providers may retain or train on data depending on their terms. The app should push provider-policy review to the user.
- Users may paste a third-party proxy URL without understanding where data goes. The disclosure should cover custom endpoints.
- Prompt injection can occur through merchant names or transaction descriptions. Treat transaction text as data, not instructions.
- API keys may leak through logs, exceptions, browser dev tools, or committed config files if not handled carefully.
- If the app presents recommendations too strongly, it may create legal/regulatory risk around financial advice.

Open questions:

- Should the first release support only local models, or support hosted providers with a stronger disclosure?
- Should full transaction-level data ever be sent, or should the feature start with summaries only?
- Should AI result history be stored locally, and if so, should it include raw model responses?
- Should API keys be environment-only for v1?
- Which insight types are most useful for the first release?
- Should users be able to preview and edit the exact data packet before sending it?
- Should the app support provider-specific privacy notes, or avoid this because provider terms change frequently?
