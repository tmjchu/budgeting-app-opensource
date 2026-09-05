// Development-only visual fixtures; not imported by the application entrypoint.
import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { AccountLogo } from '../src/components/AccountLogo';
import { institutionBrands } from '../src/lib/institutions';
import type { Account } from '../src/lib/types';
import '../src/styles.css';

const original = institutionBrands[0].logoSrc;
const account = (name: string, institutionName: string | null, type = 'credit'): Account => ({ accountId: name, name, institutionName, type, tracked: true });
const fixtures = [
  ...institutionBrands.map(brand => account(`${brand.displayName} account`, brand.displayName)),
  account('Unknown credit card', 'Neighborhood Credit Union'),
  account('Account without institution metadata', null, 'depository'),
  account('', null, 'investment'),
  account('A long account nickname that should wrap while the logo stays aligned', 'Chase', 'depository')
];
function LogoQa() {
  const [broken, setBroken] = useState(false);
  return <main style={{ display: 'block', maxWidth: 650, margin: '24px auto', padding: 16 }}>
    <h1>Account logo QA</h1>
    <p>External requests are blocked by this fixture's content security policy.</p>
    <button onClick={() => { institutionBrands[0].logoSrc = broken ? original : '/tests/intentionally-missing-logo.svg'; setBroken(!broken); }}>
      {broken ? 'Restore Chase logo' : 'Break Chase logo'}
    </button>
    <section className="panel" style={{ marginTop: 16 }}>
      {fixtures.map((fixture, index) => <div className="account-line" key={index}>
        <AccountLogo account={fixture} />
        <div style={{ minWidth: 0, overflowWrap: 'anywhere' }}><strong>{fixture.name || 'Unnamed account'}</strong><p>{fixture.type}</p></div>
        <span className="status-pill tracked">Tracked</span>
      </div>)}
    </section>
  </main>;
}
createRoot(document.getElementById('root')!).render(<LogoQa />);
