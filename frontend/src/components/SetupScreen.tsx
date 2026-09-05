import { useState, type FormEvent } from 'react';
import type { ConfigureCredentialsInput, PlaidCredentialInput, SetupStatus } from '../lib/types';

type SetupScreenProps = {
  onValidate: (credentials: PlaidCredentialInput) => Promise<string>;
  onConfigure: (credentials: ConfigureCredentialsInput) => Promise<SetupStatus>;
  onConfigured: (status: SetupStatus) => void;
};

export function SetupScreen({ onValidate, onConfigure, onConfigured }: SetupScreenProps) {
  const [clientId, setClientId] = useState('');
  const [secret, setSecret] = useState('');
  const [environment, setEnvironment] = useState<'SANDBOX' | 'PROD'>('SANDBOX');
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [encryptCsvData, setEncryptCsvData] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [isValidating, setIsValidating] = useState(false);

  const credentials = { clientId: clientId.trim(), secret: secret.trim(), environment };

  async function validate() {
    setIsValidating(true);
    setError(null);
    setMessage(null);
    try {
      setMessage(await onValidate(credentials));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to validate credentials.');
    } finally {
      setIsValidating(false);
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setMessage(null);
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

  const busy = isSaving || isValidating;

  return (
    <main className="onboarding-shell">
      <section className="onboarding-card">
        <div className="onboarding-brand">OB</div>
        <p className="eyebrow">First-run setup</p>
        <h1>Connect Open Budget to Plaid</h1>
        <p className="onboarding-copy">
          Your Plaid credentials and financial data stay on this Mac. Choose below whether to encrypt them with a password.
        </p>
        <form className="onboarding-form" onSubmit={submit}>
          <label>
            Plaid client ID
            <input value={clientId} onChange={(event) => setClientId(event.target.value)} autoComplete="off" />
          </label>
          <label>
            Plaid secret
            <input type="password" value={secret} onChange={(event) => setSecret(event.target.value)} autoComplete="off" />
          </label>
          <label>
            Plaid environment
            <select value={environment} onChange={(event) => setEnvironment(event.target.value as 'SANDBOX' | 'PROD')}>
              <option value="SANDBOX">Sandbox</option>
              <option value="PROD">Production</option>
            </select>
          </label>
          <div className="onboarding-actions compact">
            <button className="button secondary" type="button" onClick={validate} disabled={busy || !clientId || !secret}>
              {isValidating ? 'Checking…' : 'Check credentials'}
            </button>
            {message && <span className="field-success">{message}</span>}
          </div>
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
              <strong>Encrypt my local financial data</strong>
              <small>
                Optional. When selected, your Plaid credentials and CSV files are encrypted with your password.
                {' '}Leave it off to save credentials and CSV files unencrypted, without a password.
                {' '}Enabling encryption retains existing CSV files as unencrypted recovery backups.
              </small>
            </span>
          </label>
          <label>
            Encryption password
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="new-password" disabled={!encryptCsvData || busy} required={encryptCsvData} minLength={12} />
          </label>
          <label>
            Confirm password
            <input type="password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} autoComplete="new-password" disabled={!encryptCsvData || busy} required={encryptCsvData} />
          </label>
          {encryptCsvData && <p className="onboarding-copy">Use at least 12 characters. Your password is never saved and cannot be recovered.</p>}
          {error && <div className="notice error">{error}</div>}
          <button className="button primary onboarding-submit" type="submit" disabled={busy}>
            {isSaving ? 'Saving setup…' : 'Save and continue'}
          </button>
        </form>
      </section>
    </main>
  );
}
