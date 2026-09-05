import type {
  Account,
  BalanceSnapshot,
  CategoryStats,
  MonthlyStats,
  ConfigureCredentialsInput,
  PlaidCredentialInput,
  SetupStatus,
  SyncResult,
  Transaction,
  TransactionQuery
} from './types';
import { mockApi } from './mockData';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
export const isMockMode = import.meta.env.VITE_MOCK_MODE === 'true';

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
    let message = text;
    try {
      message = (JSON.parse(text) as { message?: string }).message ?? text;
    } catch {
      // Keep non-JSON backend errors readable.
    }
    throw new Error(message || `Request failed with ${response.status}`);
  }

  return response.json() as Promise<T>;
}

const liveApi = {
  getSetupStatus: async () => request<SetupStatus>('/api/setup/status'),
  validateCredentials: async (credentials: PlaidCredentialInput) =>
    request<{ message: string }>('/api/setup/validate', {
      method: 'POST',
      body: JSON.stringify(credentials)
    }),
  configureCredentials: async (credentials: ConfigureCredentialsInput) =>
    request<SetupStatus>('/api/setup/credentials', {
      method: 'POST',
      body: JSON.stringify(credentials)
    }),
  unlock: async (password: string) =>
    request<SetupStatus>('/api/setup/unlock', {
      method: 'POST',
      body: JSON.stringify({ password })
    }),
  lock: async () => request<SetupStatus>('/api/setup/lock', { method: 'POST' }),
  createLinkToken: async () => request<{ linkToken: string }>('/api/plaid/link-token', { method: 'POST' }),
  exchangePublicToken: async (publicToken: string, metadata: PlaidSuccessMetadata) =>
    request<Account[]>('/api/plaid/exchange-public-token', {
      method: 'POST',
      body: JSON.stringify({
        publicToken,
        institutionName: metadata.institution?.name,
        institutionId: metadata.institution?.institution_id,
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
  getTransactions: async (query: string | TransactionQuery) =>
    request<Transaction[]>(`/api/transactions${transactionQuery(query)}`),
  getMonthlyStats: async (month: string) => request<MonthlyStats>(`/api/stats/monthly?month=${month}`),
  getCategoryStats: async (month: string) => request<CategoryStats[]>(`/api/stats/categories?month=${month}`),
  getBalanceSnapshots: async () => request<BalanceSnapshot[]>('/api/balances/snapshots')
};

export const api = isMockMode ? mockApi : liveApi;

function transactionQuery(query: string | TransactionQuery) {
  const params = new URLSearchParams();
  if (typeof query === 'string') {
    params.set('month', query);
  } else {
    for (const [key, value] of Object.entries(query)) {
      if (value != null && value !== '') {
        params.set(key, value);
      }
    }
  }
  const queryString = params.toString();
  return queryString ? `?${queryString}` : '';
}
