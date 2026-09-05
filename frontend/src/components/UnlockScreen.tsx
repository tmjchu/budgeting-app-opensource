import { useState, type FormEvent } from 'react';
import type { SetupStatus } from '../lib/types';

type UnlockScreenProps = {
  onUnlock: (password: string) => Promise<SetupStatus>;
  onUnlocked: (status: SetupStatus) => void;
};

export function UnlockScreen({ onUnlock, onUnlocked }: UnlockScreenProps) {
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isUnlocking, setIsUnlocking] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setIsUnlocking(true);
    try {
      onUnlocked(await onUnlock(password));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to unlock Open Budget.');
    } finally {
      setIsUnlocking(false);
    }
  }

  return (
    <main className="onboarding-shell">
      <section className="onboarding-card unlock-card">
        <div className="onboarding-brand">OB</div>
        <p className="eyebrow">Welcome back</p>
        <h1>Unlock Open Budget</h1>
        <p className="onboarding-copy">Enter your password to decrypt your saved Plaid credentials for this session.</p>
        <form className="onboarding-form" onSubmit={submit}>
          <label>
            Password
            <input autoFocus type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" />
          </label>
          {error && <div className="notice error">{error}</div>}
          <button className="button primary onboarding-submit" type="submit" disabled={isUnlocking || !password}>
            {isUnlocking ? 'Unlocking…' : 'Unlock'}
          </button>
        </form>
      </section>
    </main>
  );
}
