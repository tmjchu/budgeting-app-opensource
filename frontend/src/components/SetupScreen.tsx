import { useRef, useState, type FormEvent } from 'react';
import openBudgetLogo from '../assets/open-budget-logo.png';
import plaidLogo from '../assets/plaid-logo.svg';
import type { ConfigureCredentialsInput, SetupStatus } from '../lib/types';

type SetupScreenProps = {
  onConfigure: (credentials: ConfigureCredentialsInput) => Promise<SetupStatus>;
  onConfigured: (status: SetupStatus) => void;
};

export function SetupScreen({ onConfigure, onConfigured }: SetupScreenProps) {
  const helpDialog = useRef<HTMLDialogElement>(null);
  const [clientId, setClientId] = useState('');
  const [secret, setSecret] = useState('');
  const [environment, setEnvironment] = useState<'SANDBOX' | 'PROD'>('PROD');
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [encryptCsvData, setEncryptCsvData] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  const credentials = { clientId: clientId.trim(), secret: secret.trim(), environment };

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    if (!credentials.clientId || !credentials.secret) {
      setError('Plaid client ID and secret are required.');
      return;
    }
    if (encryptCsvData && password.length < 12) {
      setError('Use a password with at least 12 characters.');
      return;
    }
    if (encryptCsvData && password !== confirmation) {
      setError('Password confirmation does not match.');
      return;
    }
    setIsSaving(true);
    try {
      onConfigured(await onConfigure({ ...credentials, ...(encryptCsvData ? { password } : {}), encryptCsvData }));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to save credentials.');
    } finally {
      setIsSaving(false);
    }
  }

  const busy = isSaving;

  return (
    <main className="onboarding-shell">
      <section className="onboarding-card">
        <img className="onboarding-logo" src={openBudgetLogo} alt="Open Budget" width={72} height={72} />
        <p className="eyebrow">First-run setup</p>
        <h1>Setup your Open Budget</h1>
        <p className="onboarding-copy">
          We use <img className="plaid-inline-logo" src={plaidLogo} alt="Plaid" /> to fetch your financial data through Open Banking.
          {' '}Open Budget stores this data only on this computer.
        </p>
        <button className="setup-help-button" type="button" aria-haspopup="dialog" onClick={() => helpDialog.current?.showModal()}>
          <span aria-hidden="true">?</span> Need help getting your Plaid credentials?
        </button>
        <dialog className="setup-help-dialog" ref={helpDialog} aria-labelledby="plaid-help-title">
          <div className="setup-help-heading">
            <h2 id="plaid-help-title">Get your Plaid credentials</h2>
            <button className="setup-help-close" type="button" aria-label="Close help" autoFocus onClick={() => helpDialog.current?.close()}>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
                <path d="M4 4L12 12M12 4L4 12" />
              </svg>
            </button>
          </div>
          <p>You’ll need a Plaid developer account to connect your financial accounts to Open Budget.</p>
          <ol>
            <li><strong>Create an account.</strong> Open the Plaid Dashboard using the button below and choose the sign-up option. Already have an account? Sign in.</li>
            <li><strong>Complete Plaid’s account setup.</strong> Verify your email and follow the dashboard prompts. To use real financial data, request Production access, including the Trial plan if available to you.</li>
            <li><strong>Find your API keys.</strong> In the dashboard, open Developers → Keys. Copy your client ID and the secret for the environment you want to use.</li>
            <li><strong>Return to Open Budget.</strong> Paste the client ID and secret into the fields below. Production is the default. If you’re using a Sandbox secret for test data, turn on Enable Sandbox. Click Save and continue to validate and save your credentials.</li>
          </ol>
          <a className="button primary" href="https://dashboard.plaid.com/signin" target="_blank" rel="noopener noreferrer">Open Plaid Dashboard <span className="visually-hidden">(opens in a new tab)</span><span aria-hidden="true">↗</span></a>
        </dialog>
        <form className="onboarding-form" onSubmit={submit}>
          <label>
            Plaid client ID
            <input value={clientId} onChange={(event) => setClientId(event.target.value)} autoComplete="off" />
          </label>
          <label>
            Plaid secret
            <input type="password" value={secret} onChange={(event) => setSecret(event.target.value)} autoComplete="off" />
          </label>
          <label className="sandbox-option">
            <span>
              <strong>Enable Sandbox</strong>
              <small>{environment === 'SANDBOX' ? 'Sandbox is on. Use test credentials and sample financial data.' : 'Production is on. Connect your real financial accounts.'}</small>
            </span>
            <input
              type="checkbox"
              role="switch"
              aria-label="Enable Sandbox"
              checked={environment === 'SANDBOX'}
              disabled={busy}
              onChange={(event) => {
                setEnvironment(event.target.checked ? 'SANDBOX' : 'PROD');
                setError(null);
              }}
            />
          </label>
          <label className="encryption-option">
            <input
              type="checkbox"
              checked={encryptCsvData}
              disabled={busy}
              onChange={(event) => {
                setEncryptCsvData(event.target.checked);
                setError(null);
                if (!event.target.checked) {
                  setPassword('');
                  setConfirmation('');
                }
              }}
            />
            <span>
              <strong>(Optional) Encrypt my local financial data</strong>
              <small>
                When selected, your Plaid credentials and financial data are encrypted with your password.
                {' '}Leave it off to save credentials and financial data unencrypted.
              </small>
              <small>
                (NOTE) We have no password recovery or reset process. If you forget your password, you won’t be able to decrypt your data.
              </small>
            </span>
          </label>
          {encryptCsvData && (
            <>
              <label>
                Encryption password
                <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="new-password" disabled={busy} required minLength={12} />
              </label>
              <label>
                Confirm password
                <input type="password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} autoComplete="new-password" disabled={busy} required />
              </label>
              <p className="onboarding-copy">Use at least 12 characters. Your password is never saved and cannot be recovered.</p>
            </>
          )}
          {error && <div className="notice error">{error}</div>}
          <button className="button primary onboarding-submit" type="submit" disabled={busy}>
            {isSaving ? 'Saving setup…' : 'Save and continue'}
          </button>
        </form>
      </section>
    </main>
  );
}
