# Mac App First-Run Setup And Data Encryption

Date: 2026-07-16
Status: Draft

## Goal

Make the desktop Mac app usable on first launch without requiring terminal-provided environment variables, while protecting Plaid secrets and optionally protecting local CSV data with a user-provided password.

The app should:

- Start the UI even when Plaid credentials have not been configured yet.
- Prompt new users for `PLAID_CLIENT_ID`, `PLAID_SECRET`, and a password.
- Use the password to encrypt and decrypt saved Plaid secrets.
- Give users an explicit option to encrypt and decrypt local CSV files using the same password as the key material.
- Preserve the local-first model and avoid external secret storage or hosted dependencies.

## Context

The current app has a Java Spring Boot backend, React/Vite frontend, and an Electron desktop prototype. Today, the backend requires `PLAID_CLIENT_ID` and `PLAID_SECRET` at startup, which means the desktop app can fail before the UI is available. That is poor first-run behavior for a Mac app.

The backend currently persists financial data as CSV files under a configurable data directory. Plaid credentials are read from environment variables. The new desktop-first flow should move user configuration into app-managed local data while still allowing development overrides through environment variables.

## Scope

In scope:

- Backend support for starting without Plaid credentials.
- Backend APIs for setup status, credential configuration, credential validation, lock/unlock, and encryption operations.
- Password-based encryption for saved Plaid credentials.
- Optional encryption/decryption of CSV files.
- Frontend onboarding, unlock, and data-security controls.
- Electron launcher updates so the UI always opens and points at the local backend.
- Migration behavior from existing environment-variable credentials and plaintext CSV files.
- Tests for crypto behavior, setup flow, and CSV encryption/decryption.

Out of scope for the first implementation:

- Cloud sync.
- OS Keychain integration.
- Multi-user account management.
- Password recovery if the password is forgotten.
- Automatic Plaid credential provisioning.
- Full notarized macOS release pipeline.

## Proposed Changes

### Startup Behavior

Change the backend so Plaid integration is not eagerly constructed in a way that prevents the app from starting. The backend should boot in one of these states:

- `needs_setup`: no encrypted Plaid credentials exist and no usable environment credentials are present.
- `locked`: encrypted credentials exist but the password has not been entered in this app session.
- `ready`: credentials are available in memory and API operations can use Plaid.

The Electron app should start the backend and load the UI regardless of credential state. The React app should call a setup-status endpoint and route the user to setup, unlock, or dashboard.

### Plaid Credential Storage

Create an app-managed encrypted secrets file under the configured data directory, for example:

```text
data/secrets.json.enc
```

Store only encrypted secret payloads on disk. The plaintext payload can contain:

```json
{
  "plaidClientId": "...",
  "plaidSecret": "...",
  "plaidEnvironment": "sandbox",
  "createdAt": "...",
  "updatedAt": "..."
}
```

Use password-based authenticated encryption:

- Derive an encryption key from the user password using Argon2id if a mature dependency is acceptable, otherwise PBKDF2-HMAC-SHA-256 with a high iteration count.
- Use a unique random salt per encrypted file.
- Use AES-256-GCM for authenticated encryption.
- Store metadata with the ciphertext: version, KDF name, KDF parameters, salt, IV/nonce, and auth tag.
- Never log plaintext secrets, passwords, derived keys, or decrypted payloads.

Keep decrypted Plaid credentials only in process memory after unlock. Do not write plaintext credentials to environment files.

### Password Handling

The setup screen should require:

- Plaid client ID.
- Plaid secret.
- Password.
- Password confirmation.
- Optional Plaid environment selection, defaulting to sandbox.

The unlock screen should require:

- Password.

Password rules should be clear and local:

- Minimum length, likely 12 characters.
- Show a confirmation mismatch error.
- Explain that there is no recovery if the password is lost.

The backend should verify password correctness by attempting authenticated decryption of the encrypted secrets file. It should expose only success or failure, not detailed crypto errors.

### CSV Encryption Option

Add an explicit data-security section in the UI with options:

- Encrypt local CSV data.
- Decrypt local CSV data.
- Show current data encryption status.

Recommended first implementation:

- Encrypt each CSV file into a matching encrypted file and remove or archive the plaintext only after successful encryption.
- Use a recognizable extension, for example:

```text
accounts.csv.enc
transactions.csv.enc
categories.csv.enc
balance_snapshots.csv.enc
plaid_items.csv.enc
sync_runs.csv.enc
```

- Use a unique random salt and IV per encrypted CSV file.
- Store crypto metadata per file.
- On backend startup, detect whether CSV storage is plaintext or encrypted.
- If encrypted CSV files exist and the app is locked, dashboard data endpoints should return a locked/setup-required response rather than failing generically.
- After unlock, repositories should be able to read decrypted CSV content and write back encrypted content.

There are two viable implementation models:

1. File-level decrypt-on-read/encrypt-on-write adapter.
2. Session unlock that decrypts files to a private temporary working directory and re-encrypts on mutation/shutdown.

Prefer model 1 if the CSV repository layer can be wrapped cleanly. Prefer model 2 only if repository changes become too invasive, and ensure temporary plaintext files are deleted on shutdown.

### Migration

Support these migration paths:

- New user: no credentials, no CSV data. Show setup.
- Existing developer/user with environment Plaid credentials: allow the app to start as `ready`, then offer to save credentials encrypted.
- Existing plaintext CSV data: continue reading it, show `CSV data is not encrypted`, and offer encryption.
- Existing encrypted secrets: start as `locked` until password is entered.
- Existing encrypted CSV files: require unlock before data access.

Avoid automatic destructive migration. For CSV encryption/decryption:

- Write new encrypted/decrypted files first.
- Verify they can be read.
- Rename originals only after verification.
- Keep a timestamped backup unless the user explicitly chooses otherwise.

### API Design

Add backend endpoints along these lines:

```text
GET  /api/setup/status
POST /api/setup/credentials
POST /api/setup/unlock
POST /api/setup/lock
POST /api/security/csv/encrypt
POST /api/security/csv/decrypt
GET  /api/security/csv/status
```

Response examples:

```json
{
  "state": "needs_setup",
  "hasEncryptedSecrets": false,
  "hasEnvironmentCredentials": false,
  "csvEncryptionStatus": "plaintext"
}
```

Use generic error messages for incorrect passwords and decryption failures. Do not echo submitted credentials back to the frontend.

### Frontend UX

Add app-level routing/state:

- Setup screen for first-run credential and password collection.
- Unlock screen for returning users.
- Dashboard screen when ready.
- Data-security panel for CSV encryption/decryption.

The UI should make the app feel like a desktop app:

- The window opens immediately.
- Setup and unlock screens are calm, direct, and form-focused.
- Password fields use password inputs.
- Encryption/decryption actions show progress and final status.
- Risky operations require confirmation.
- Errors explain what the user can do next without exposing internal stack traces.

### Electron Integration

Update the desktop launcher so it:

- Starts the backend without requiring Plaid env vars.
- Sets `BUDGET_DATA_DIRECTORY` to the app support directory.
- Loads the UI once the backend health/setup endpoint responds.
- Does not store Plaid secrets in Electron config files.
- Optionally opens dev tools only in development mode.

The frontend should use the backend setup-status API after load and not assume the dashboard is ready.

## Files And Areas To Inspect

Backend:

- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/localbudget/app/config/BudgetAppProperties.java`
- `backend/src/main/java/com/localbudget/app/config/WebConfig.java`
- `backend/src/main/java/com/localbudget/app/gateway/plaid/api/PlaidSdkGateway.java`
- `backend/src/main/java/com/localbudget/app/gateway/plaid/api/PlaidGateway.java`
- `backend/src/main/java/com/localbudget/app/data/repository/*CsvRepository.java`
- `backend/src/main/java/com/localbudget/app/data/repository/CsvSupport.java`
- `backend/src/main/java/com/localbudget/app/api/controller/*`
- `backend/src/test/java/com/localbudget/app/*`

Frontend:

- `frontend/src/App.tsx`
- `frontend/src/lib/api.ts`
- `frontend/src/lib/types.ts`
- `frontend/src/styles.css`
- New setup/unlock/security components under `frontend/src/components`

Desktop:

- `desktop/src/main.cjs`
- `desktop/package.json`
- `README.md`

## Execution Steps

1. Introduce backend setup state.
   - Add a small service that can answer whether the app is `needs_setup`, `locked`, or `ready`.
   - Add setup-status API endpoint.
   - Make the backend start without Plaid credentials.

2. Refactor Plaid gateway creation.
   - Avoid constructing `PlaidSdkGateway` with missing credentials.
   - Route Plaid operations through a credentials provider that can return environment credentials or decrypted saved credentials.
   - Return a clear setup/locked error if Plaid operations are attempted before credentials are available.

3. Add encrypted secrets storage.
   - Implement crypto envelope model and serialization.
   - Implement password-based key derivation.
   - Implement save, unlock, lock, and status behavior.
   - Add tests for successful decrypt, wrong password, tampered ciphertext, and metadata validation.

4. Add setup and unlock APIs.
   - `POST /api/setup/credentials` saves encrypted credentials and unlocks the current session.
   - `POST /api/setup/unlock` decrypts existing credentials into memory.
   - `POST /api/setup/lock` clears decrypted credentials from memory.
   - Validate inputs and avoid returning secrets.

5. Add CSV encryption/decryption support.
   - Decide adapter model after inspecting repository boundaries.
   - Implement file-level encryption metadata and safe rename/backup flow.
   - Add status detection for plaintext, encrypted, mixed, and missing data.
   - Add encrypt/decrypt APIs.
   - Add tests with temporary data directories.

6. Update frontend setup flow.
   - On app load, call setup-status.
   - Render setup screen for new users.
   - Render unlock screen for locked users.
   - Render dashboard only when ready.
   - Add client-side validation for password confirmation.

7. Add frontend data-security controls.
   - Show CSV encryption status.
   - Add encrypt/decrypt buttons with confirmation.
   - Disable actions while requests are in flight.
   - Display success/failure messages.

8. Update Electron startup.
   - Remove hard failure on missing Plaid credentials.
   - Wait on setup-status or health endpoint.
   - Keep dynamic backend/frontend ports.
   - Verify development and packaged behavior.

9. Update documentation.
   - Explain first-run setup.
   - Explain password loss implications.
   - Explain where encrypted files live.
   - Explain how to migrate existing environment credentials and CSV data.

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

Desktop:

```bash
cd desktop
npm run dev
npm run dist:mac
```

Manual verification scenarios:

- Fresh data directory opens setup UI.
- Setup with Plaid credentials and password reaches dashboard.
- Restart app with encrypted credentials shows unlock UI.
- Wrong password fails without revealing details.
- Correct password unlocks dashboard.
- CSV encryption converts plaintext CSV files to encrypted files.
- Encrypted CSV data can be read after unlock.
- CSV decryption restores readable CSV files.
- Dashboard does not start Plaid calls before setup/unlock is complete.
- Packaged Mac app opens UI without terminal environment variables.

Security verification:

- No plaintext Plaid secrets appear in saved files.
- No plaintext passwords or derived keys appear in logs.
- Tampering with encrypted secrets causes unlock failure.
- Tampering with encrypted CSV files causes read/decrypt failure.
- Backups are created before destructive CSV migration steps.

## Risks And Questions

- Password loss means encrypted secrets and encrypted CSV data cannot be recovered. The UI and docs must say this clearly.
- Java crypto dependency choice needs a decision. Argon2id is preferred for password hashing/KDF, but PBKDF2 may be simpler with the JDK alone.
- CSV encryption can conflict with the current repository model if repositories assume direct file paths. Inspect before choosing decrypt-on-read versus temporary workspace.
- Plaid Link itself still needs internet access and valid Plaid credentials. The app can start without credentials, but connecting accounts cannot work until setup is complete.
- macOS app signing/notarization remains separate from this plan.
- It may be worth adding OS Keychain support later, but the requested password-derived local encryption should be implemented first.
