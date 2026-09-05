# Future Development Plan

This document captures the long-term direction for Open Budget so future development can refer back to the intended product shape before adding features. The focus is to keep the app local-first, transparent, and practical while adding more of the categorization, budgeting, and transaction editing workflows commonly found in personal finance apps such as Rocket Money.

## Product Direction

Open Budget should evolve from a read-only spending dashboard into a lightweight personal budgeting tool where the user can correct, organize, and plan around their own financial data.

The app should continue to prioritize:

- Local ownership of financial data.
- Clear, inspectable CSV-backed persistence unless the storage model intentionally changes.
- Simple budgeting workflows that do not require a paid third-party budgeting service.
- Manual overrides that survive future Plaid syncs.
- Reporting that explains where money went and whether spending is aligned with the user's plan.

## Current Foundations

The existing app already has several foundations that support this plan:

- Transactions are synced from Plaid and persisted locally.
- Transactions have Plaid-provided `primaryCategory` and `detailedCategory` values.
- Transactions already support a `localCategory` field and an `effectiveCategory()` concept.
- The merge process preserves local category and exclusion edits during future syncs.
- Transfers are automatically excluded by the current transaction rule service.
- Dashboard metrics and category stats are already calculated from local transaction data.
- Transaction queries already support month, date range, account, and category filtering.

Future work should extend these foundations rather than replacing them without a clear reason.

## Feature Goals

### 1. Custom Categories

The user should be able to define their own category list instead of relying only on categories supplied by Plaid.

Target capabilities:

- Create, rename, archive, and reorder custom categories.
- Assign categories to transactions manually.
- Keep custom categories separate from Plaid categories while still allowing Plaid categories to be used as defaults.
- Preserve custom category assignments across future Plaid syncs.
- Prevent accidental data loss when renaming or archiving a category already used by transactions.

Possible data additions:

- `categories.csv`
- Category id, display name, optional color, optional parent category, archived flag, sort order, created timestamp, updated timestamp.

Important behavior:

- Existing transactions with `localCategory` should continue to work.
- A future category model should prefer stable category ids over display names for long-term safety.
- Archived categories should remain visible in historical reporting when historical transactions use them.

### 2. Custom Transaction Categorization Rules

The user should be able to define rules that automatically categorize transactions during sync or during a manual recategorization pass.

Target capabilities:

- Create rules based on transaction name, merchant name, amount, account, Plaid category, payment channel, or description text.
- Support rule match types such as contains, exact match, starts with, regular expression, amount range, and account match.
- Assign a custom category when a rule matches.
- Optionally rename the transaction when a rule matches.
- Optionally exclude the transaction from budgets and spending metrics when a rule matches.
- Preview how many existing transactions a rule would affect before saving it.
- Apply a rule retroactively to historical transactions.
- Order rules so more specific rules can run before broad fallback rules.

Possible data additions:

- `transaction_rules.csv`
- Rule id, name, enabled flag, priority, match field, match operator, match value, target category id, target display name, exclusion action, apply-to-existing flag, created timestamp, updated timestamp.

Important behavior:

- Manual edits should have precedence over automatic rules unless the user explicitly chooses to overwrite them.
- Rule results should be explainable in the UI so the user can see why a transaction was categorized a certain way.
- Rule application should be deterministic so a sync produces predictable results.

### 3. Metrics Per Filter On Categories

The user should be able to filter spending views and receive metrics scoped to the selected filters.

Target capabilities:

- Filter metrics by category, account, date range, month, custom date period, pending status, excluded status, and transaction search text.
- Show income, spending, net cash flow, transaction count, and category breakdown for the active filter.
- Compare filtered spending against the matching budget amount when a budget exists.
- Preserve useful dashboard views through URL query parameters or saved view state.

Possible API additions:

- Extend stats endpoints to accept the same filters as transaction queries.
- Consider a shared query model so transaction lists and metrics cannot drift apart.

Important behavior:

- Metrics and transaction lists should always agree for the same filter.
- Excluded transactions should remain hidden from spending metrics by default, with an explicit option to include them.
- Pending transactions should be visible but clearly marked, and future budget calculations should decide whether pending spending is included by default.

### 4. Monthly Budget Amounts

The user should be able to set monthly budget amounts and compare actual spending against the plan.

Target capabilities:

- Set an overall monthly spending budget.
- Set monthly budgets per category.
- Copy a previous month's budget into a new month.
- Edit a budget without changing historical months unintentionally.
- Show spent amount, remaining amount, and percentage used.
- Highlight over-budget categories.
- Support months with no explicit budget by using either no budget or an optional default recurring budget.

Possible data additions:

- `budgets.csv`
- Budget id, month, category id, budget amount, currency, created timestamp, updated timestamp.

Important behavior:

- Budgets should use the same effective category model as transaction reporting.
- Category budgets should be based on spending transactions, not income.
- The app should be careful with sign conventions because Plaid spending is currently represented as positive amounts and income as negative amounts.

### 5. Custom Date Assignment To Transactions

The user should be able to assign a custom reporting date to a transaction when the Plaid date is not the date they want for budgeting.

Target capabilities:

- Edit a transaction's budget/reporting date.
- Keep the original Plaid transaction date for auditability.
- Use the custom date in monthly reporting, category metrics, and budgets.
- Show both original date and custom date when they differ.
- Preserve custom dates across future Plaid syncs.

Possible data additions:

- Add a `localDate` or `budgetDate` field to transaction persistence.

Important behavior:

- Reporting should use an `effectiveDate`, similar to the existing `effectiveCategory`.
- Sync should continue updating Plaid-sourced fields while preserving the local reporting date.
- The UI should make it clear when a transaction has been moved into a different reporting month.

### 6. Custom Naming Of Transactions

The user should be able to rename transactions for readability while preserving the original Plaid-provided values.

Target capabilities:

- Edit a transaction display name.
- Keep original Plaid name and merchant name for traceability.
- Use the custom display name in transaction lists, search, filters, and reporting drilldowns.
- Preserve custom names across future Plaid syncs.
- Allow rules to apply automatic display names for recurring merchants.

Possible data additions:

- Add a `localName` or `displayNameOverride` field to transaction persistence.

Important behavior:

- Reporting should use an `effectiveName`, similar to the existing `effectiveCategory`.
- Search should consider original name, merchant name, and custom display name.
- The UI should make edited names visually understandable without being noisy.

### 7. AI Model Integration For Budgeting Suggestions

As a final planned expansion, the app should support an opt-in AI integration that can generate budgeting suggestions from the user's local financial data.

The detailed implementation plan lives in [AI Model Integration For Budgeting Suggestions](./2026-07-01%20AI%20model%20integration%20for%20budgeting%20suggestions.md).

Target capabilities:

- Let the user configure an AI provider, compatible endpoint, and model.
- Require explicit consent before any banking data or derived financial summaries are sent to an AI provider.
- Compile bounded financial context from transactions, categories, budgets, balances, and filters.
- Use static, versioned prompt templates for budgeting suggestions.
- Return budgeting suggestions, category cleanup ideas, subscription observations, savings opportunities, and spending review summaries.
- Display provider, model, prompt version, date range, and timestamp with each AI insight run.
- Make it clear that AI output is budgeting guidance only, not financial, investment, tax, legal, credit, or insurance advice.

Important behavior:

- AI sharing must be disabled by default.
- The app should send the smallest useful data summary rather than raw transaction history by default.
- The AI gateway should be provider-neutral and isolated behind backend interfaces, similar to Plaid.
- Secrets must not be committed, logged, or exposed through browser-visible errors.
- AI should build on the cleaned-up budget data from earlier milestones rather than replace user-controlled categories, rules, and budgets.

## Suggested Milestones

### Milestone 1: Durable Local Overrides

Goal: Make transaction edits first-class and sync-safe.

Scope:

- Add effective date and effective name concepts.
- Preserve local category, excluded flag, custom date, and custom name during sync.
- Add APIs for updating editable transaction fields.
- Add focused tests for merge preservation and reporting behavior.

Outcome:

- The user can safely correct individual transactions without those corrections being lost later.

### Milestone 2: Category Management

Goal: Move from ad hoc category strings to managed custom categories.

Scope:

- Add category persistence.
- Add category CRUD endpoints.
- Update transaction editing to use managed categories.
- Keep backward compatibility for existing `localCategory` values.
- Add category UI controls in the dashboard.

Outcome:

- The user owns the category system and can organize spending in their own language.

### Milestone 3: Rule-Based Automation

Goal: Reduce repeated manual cleanup.

Scope:

- Add rule persistence and a rule evaluation engine.
- Replace hard-coded transfer exclusion with configurable system/user rules.
- Add rule preview and retroactive apply behavior.
- Add tests for rule priority, matching, and manual override precedence.

Outcome:

- Recurring transactions are categorized, renamed, or excluded automatically.

### Milestone 4: Filtered Reporting

Goal: Make all metrics respond to the same filters used by the transaction list.

Scope:

- Create a shared transaction filter model.
- Extend monthly and category stats endpoints to accept account, category, date, pending, excluded, and search filters.
- Update the frontend dashboard so metric cards, category breakdowns, and transaction lists stay in sync.

Outcome:

- The dashboard becomes a true analysis tool instead of a fixed monthly summary.

### Milestone 5: Monthly Budgets

Goal: Add planning and progress tracking.

Scope:

- Add monthly and category budget persistence.
- Add budget APIs and frontend budget controls.
- Add budget progress views to category metrics.
- Add month-copy behavior.

Outcome:

- The user can set a monthly plan and quickly see what is on track or over budget.

### Milestone 6: Optional AI Budgeting Suggestions

Goal: Add an opt-in AI layer after the core budgeting system is reliable.

Scope:

- Implement the July 1 AI model integration plan.
- Add provider-neutral AI configuration and gateway interfaces.
- Add consent, disclosure, and audit metadata before any AI call is allowed.
- Build a financial context compiler that uses transactions, categories, budgets, balances, and active filters.
- Add AI Insights UI for manually requested budgeting suggestions.
- Add tests for consent enforcement, data minimization, prompt assembly, gateway behavior, and safe error handling.

Outcome:

- The user can request AI-generated budgeting suggestions while staying in control of what data is shared and which provider/model receives it.

## Technical Principles

- Preserve Plaid-sourced raw fields and store local overrides separately.
- Prefer effective accessors such as `effectiveCategory`, `effectiveDate`, and `effectiveName` for reporting.
- Keep sync idempotent and deterministic.
- Treat manual user edits as higher priority than automatic rule output.
- Keep CSV migrations explicit when adding new columns or files.
- Add tests around every field that must survive a sync.
- Keep API query behavior shared between transaction lists and metrics.

## Open Product Questions

These decisions do not need to block the document, but they should be answered before implementation:

- Should category budgets support parent and child categories, or only flat categories at first?
- Should pending transactions count against budgets by default?
- Should custom transaction dates affect only budgets or all reports?
- Should rules be allowed to overwrite manual edits, and if so should that be a one-time explicit action?
- Should budgets support rollover, where unspent money carries into the next month?
- Should the app support split transactions, where one transaction can be assigned to multiple categories?
- Should AI insight runs send summaries only, or should the user be able to opt into transaction-level detail for specific analyses?
- Should AI result history be stored locally, and if so should raw prompts and responses be excluded by default?

## Definition Of Done For This Roadmap

This long-term plan is considered achieved when the user can:

- Create and manage custom categories.
- Automatically categorize recurring transactions with user-defined rules.
- Manually edit transaction category, reporting date, display name, and exclusion status.
- Trust that local edits survive future Plaid syncs.
- Filter dashboards and receive matching metrics for the active filter.
- Set monthly budget amounts and compare actual spending against those budgets.
- Optionally connect an AI model/provider and request budgeting suggestions with explicit consent, clear disclosure, and safe data minimization.
