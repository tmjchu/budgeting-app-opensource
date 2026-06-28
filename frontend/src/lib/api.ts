import type {
  Account,
  BalanceSnapshot,
  CategoryStats,
  MonthlyStats,
  SyncResult,
  Transaction
} from './types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers
    },
    ...options
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export const api = {
  createLinkToken: async () => request<{ linkToken: string }>('/api/plaid/link-token', { method: 'POST' }),
  exchangePublicToken: async (publicToken: string, metadata: PlaidSuccessMetadata) =>
    request<Account[]>('/api/plaid/exchange-public-token', {
      method: 'POST',
      body: JSON.stringify({
        publicToken,
        institutionName: metadata.institution?.name,
        selectedAccounts: metadata.accounts.map((account) => ({
          accountId: account.id,
          name: account.name,
          mask: account.mask,
          type: account.type,
          subtype: account.subtype
        }))
      })
    }),
  sync: async () => request<SyncResult>('/api/sync', { method: 'POST' }),
  getAccounts: async () => request<Account[]>('/api/accounts'),
  getTransactions: async (month: string) => request<Transaction[]>(`/api/transactions?month=${month}`),
  getMonthlyStats: async (month: string) => request<MonthlyStats>(`/api/stats/monthly?month=${month}`),
  getCategoryStats: async (month: string) => request<CategoryStats[]>(`/api/stats/categories?month=${month}`),
  getBalanceSnapshots: async () => request<BalanceSnapshot[]>('/api/balances/snapshots')
};
