import { useState, type ReactNode } from 'react';
import type { Account } from '../lib/types';
import { accountIconKind, normalizeInstitutionName } from '../lib/institutionResolver';
import { resolveInstitutionBrand } from '../lib/institutions';

type Props = {
  account: Pick<Account, 'name' | 'type' | 'subtype' | 'institutionId' | 'institutionName'>;
  size?: number;
};

function LogoImage({ src, fallback }: { src: string; fallback: ReactNode }) {
  const [failed, setFailed] = useState(false);
  return failed ? fallback : <img src={src} alt="" onError={() => setFailed(true)} />;
}

export function AccountLogo({ account, size = 34 }: Props) {
  const brand = resolveInstitutionBrand(account);
  const institution = brand?.displayName ?? account.institutionName?.trim();
  const kind = accountIconKind(account.type, account.subtype);
  const label = institution && normalizeInstitutionName(institution) !== normalizeInstitutionName(account.name)
    ? institution : undefined;
  const fallback = (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {kind === 'bank' ? (
        <><path d="m3 9 9-6 9 6H3ZM3 21h18M5 18v-6m7 6v-6m7 6v-6" /></>
      ) : kind === 'card' ? (
        <><rect x="3" y="5" width="18" height="14" rx="3" /><path d="M3 10h18M7 15h3" /></>
      ) : (
        <><path d="M20 8V6a2 2 0 0 0-2-2H6a3 3 0 0 0 0 6h14v10H6a3 3 0 0 1-3-3V7" /><path d="M20 13h-5v4h5" /></>
      )}
    </svg>
  );

  return (
    <span className="account-logo" style={{ width: size, height: size }} role={label ? 'img' : undefined} aria-label={label} aria-hidden={label ? undefined : true} title={institution}>
      {brand ? <LogoImage key={brand.logoSrc} src={brand.logoSrc} fallback={fallback} /> : fallback}
    </span>
  );
}
