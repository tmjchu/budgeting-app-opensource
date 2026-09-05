export type InstitutionIdentity = {
  institutionId?: string | null;
  institutionName?: string | null;
};

export type InstitutionBrand = {
  key: string;
  displayName: string;
  aliases: readonly string[];
  plaidInstitutionIds: readonly string[];
  logoSrc: string;
};

// Only fold common punctuation variants; never remove words or match substrings.
export function normalizeInstitutionName(name: string): string {
  return name.normalize('NFKC').toLowerCase().replace(/[.’']/g, '').replace(/[-–—,]/g, ' ').replace(/\s+/g, ' ').trim();
}

export function createInstitutionResolver(brands: readonly InstitutionBrand[]) {
  const byId = new Map<string, InstitutionBrand>();
  const byName = new Map<string, InstitutionBrand>();
  const keys = new Set<string>();
  function register(map: Map<string, InstitutionBrand>, key: string, brand: InstitutionBrand) {
    if (!key || (map.has(key) && map.get(key) !== brand)) {
      throw new Error(`Duplicate or empty institution alias/ID: ${key}`);
    }
    map.set(key, brand);
  }
  for (const brand of brands) {
    if (keys.has(brand.key)) throw new Error(`Duplicate institution key: ${brand.key}`);
    keys.add(brand.key);
    for (const id of brand.plaidInstitutionIds) register(byId, id, brand);
    for (const name of [brand.displayName, ...brand.aliases]) {
      register(byName, normalizeInstitutionName(name), brand);
    }
  }
  return ({ institutionId, institutionName }: InstitutionIdentity): InstitutionBrand | undefined =>
    (institutionId ? byId.get(institutionId) : undefined)
    ?? (institutionName ? byName.get(normalizeInstitutionName(institutionName)) : undefined);
}

export function accountIconKind(type?: string | null, subtype?: string | null): 'bank' | 'card' | 'wallet' {
  const normalizedType = type?.toLowerCase();
  const normalizedSubtype = subtype?.toLowerCase();
  if (normalizedType === 'credit' || normalizedSubtype === 'credit card') return 'card';
  if (normalizedType === 'depository' || ['checking', 'savings'].includes(normalizedSubtype ?? '')) return 'bank';
  return 'wallet';
}
