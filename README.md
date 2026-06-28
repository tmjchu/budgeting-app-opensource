# Local Budget

Local-first budgeting app with a Java Spring Boot backend, CSV persistence, Plaid REST integration, and a React/Vite dashboard.

## Attribution

This project was originally created by Tommy. See [NOTICE](NOTICE) for attribution details.

## Run

Backend:

```bash
cd backend
PLAID_CLIENT_ID=your_client_id PLAID_SECRET=your_secret mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev -- --host 127.0.0.1
```

Open `http://127.0.0.1:5173`.

## Data Files

The backend writes local CSV data under `backend/data` by default:

- `accounts.csv`
- `plaid_items.csv`
- `transactions.csv`
- `balance_snapshots.csv`
- `sync_runs.csv`

Override the location with `BUDGET_DATA_DIRECTORY=/path/to/data`.

## Plaid

The app defaults to Plaid sandbox:

```bash
PLAID_BASE_URL=https://sandbox.plaid.com
```

Use Plaid development or production URLs only after your Plaid account and product access are configured.

## Verify

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm run build
npm audit
```
