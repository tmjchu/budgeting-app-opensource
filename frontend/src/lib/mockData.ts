import type { Account, BalanceSnapshot, CategoryStats, ConfigureCredentialsInput, MonthlyStats, SetupStatus, SyncResult, Transaction, TransactionQuery } from './types';

// Centralized sample content: edit this file to explore UI states without the backend.
const now = new Date();
const MOCK_MONTH = now.toISOString().slice(0, 7);
const day = (value: number) => `${MOCK_MONTH}-${String(value).padStart(2, '0')}`;

export const mockAccounts: Account[] = [
  { accountId: 'checking', name: 'Everyday Checking', mask: '2841', type: 'depository', subtype: 'checking', tracked: true },
  { accountId: 'savings', name: 'Rainy Day Savings', mask: '9017', type: 'depository', subtype: 'savings', tracked: true },
  { accountId: 'credit', name: 'Everyday Card', mask: '4420', type: 'credit', subtype: 'credit card', tracked: true }
];

export const mockTransactions: Transaction[] = [
  { transactionId: 't1', accountId: 'checking', accountName: 'Everyday Checking', date: day(22), name: 'PAYROLL', merchantName: 'Acme Studio', amount: -4250, category: 'Income', pending: false, excluded: false, paymentChannel: 'other' },
  { transactionId: 't2', accountId: 'credit', accountName: 'Everyday Card', date: day(21), name: 'TRADER JOES #144', merchantName: "Trader Joe's", amount: 86.42, category: 'Groceries', pending: false, excluded: false, paymentChannel: 'in store' },
  { transactionId: 't3', accountId: 'credit', accountName: 'Everyday Card', date: day(20), name: 'BLUE BOTTLE', merchantName: 'Blue Bottle Coffee', amount: 7.8, category: 'Dining', pending: true, excluded: false, paymentChannel: 'in store' },
  { transactionId: 't4', accountId: 'checking', accountName: 'Everyday Checking', date: day(18), name: 'PACIFIC GAS ELECTRIC', merchantName: 'PG&E', amount: 148.31, category: 'Utilities', pending: false, excluded: false, paymentChannel: 'online' },
  { transactionId: 't5', accountId: 'credit', accountName: 'Everyday Card', date: day(16), name: 'SHELL OIL', merchantName: 'Shell', amount: 62.1, category: 'Transportation', pending: false, excluded: false, paymentChannel: 'in store' },
  { transactionId: 't6', accountId: 'credit', accountName: 'Everyday Card', date: day(14), name: 'NETFLIX.COM', merchantName: 'Netflix', amount: 22.99, category: 'Entertainment', pending: false, excluded: false, paymentChannel: 'online' },
  { transactionId: 't7', accountId: 'checking', accountName: 'Everyday Checking', date: day(12), name: 'PROPERTY MANAGEMENT', merchantName: 'Parkside Apartments', amount: 1850, category: 'Housing', pending: false, excluded: false, paymentChannel: 'online' },
  { transactionId: 't8', accountId: 'credit', accountName: 'Everyday Card', date: day(10), name: 'WHOLEFDS MKT', merchantName: 'Whole Foods', amount: 113.72, category: 'Groceries', pending: false, excluded: false, paymentChannel: 'in store' },
  { transactionId: 't9', accountId: 'credit', accountName: 'Everyday Card', date: day(8), name: 'UBER TRIP', merchantName: 'Uber', amount: 31.24, category: 'Transportation', pending: false, excluded: false, paymentChannel: 'online' },
  { transactionId: 't10', accountId: 'credit', accountName: 'Everyday Card', date: day(6), name: 'OUTDOOR SUPPLY', merchantName: 'Outdoor Supply Co.', amount: 274.5, category: 'Shopping', pending: false, excluded: false, paymentChannel: 'online' },
  { transactionId: 't11', accountId: 'checking', accountName: 'Everyday Checking', date: day(4), name: 'TRANSFER TO SAVINGS', amount: 500, category: 'Transfer', pending: false, excluded: true, paymentChannel: 'other' },
  { transactionId: 't12', accountId: 'credit', accountName: 'Everyday Card', date: day(2), name: 'SWEETGREEN', merchantName: 'Sweetgreen', amount: 18.65, category: 'Dining', pending: false, excluded: false, paymentChannel: 'in store' }
];

export const mockBalances: BalanceSnapshot[] = [
  { snapshotId: 'b1', syncedAt: now.toISOString(), accountId: 'checking', accountName: 'Everyday Checking', accountMask: '2841', currentBalance: 6248.72, availableBalance: 5948.72, isoCurrencyCode: 'USD' },
  { snapshotId: 'b2', syncedAt: now.toISOString(), accountId: 'savings', accountName: 'Rainy Day Savings', accountMask: '9017', currentBalance: 18420, availableBalance: 18420, isoCurrencyCode: 'USD' },
  { snapshotId: 'b3', syncedAt: now.toISOString(), accountId: 'credit', accountName: 'Everyday Card', accountMask: '4420', currentBalance: 1248.66, availableBalance: 3751.34, isoCurrencyCode: 'USD' }
];

function matchingTransactions(query: string | TransactionQuery): Transaction[] {
  const filters = typeof query === 'string' ? { month: query } : query;
  return mockTransactions.filter((transaction) =>
    (!filters.month || transaction.date.startsWith(filters.month)) &&
    (!filters.startDate || transaction.date >= filters.startDate) &&
    (!filters.endDate || transaction.date <= filters.endDate) &&
    (!filters.accountId || transaction.accountId === filters.accountId) &&
    (!filters.category || transaction.category === filters.category));
}

function statsFor(month: string): MonthlyStats {
  const rows = matchingTransactions({ month }).filter((transaction) => !transaction.excluded);
  const income = rows.filter((row) => row.amount < 0).reduce((total, row) => total + Math.abs(row.amount), 0);
  const spending = rows.filter((row) => row.amount > 0).reduce((total, row) => total + row.amount, 0);
  return { month, income, spending, netCashFlow: income - spending, transactionCount: rows.length };
}

function categoriesFor(month: string): CategoryStats[] {
  const totals = new Map<string, CategoryStats>();
  for (const row of matchingTransactions({ month }).filter((item) => item.amount > 0 && !item.excluded)) {
    const current = totals.get(row.category) ?? { category: row.category, amount: 0, transactionCount: 0 };
    current.amount += row.amount;
    current.transactionCount += 1;
    totals.set(row.category, current);
  }
  return [...totals.values()].sort((left, right) => right.amount - left.amount);
}

const wait = <T>(value: T) => new Promise<T>((resolve) => window.setTimeout(() => resolve(value), 120));

export const mockApi = {
  getSetupStatus: async () => wait({ state: 'ready' as const, hasEncryptedSecrets: false, hasEnvironmentCredentials: false, csvEncryptionStatus: 'plaintext' as const }),
  validateCredentials: async () => wait({ message: 'Plaid credentials are valid.' }),
  configureCredentials: async (input: ConfigureCredentialsInput): Promise<SetupStatus> => wait({ state: 'ready', hasEncryptedSecrets: input.encryptCsvData, hasEnvironmentCredentials: false, csvEncryptionStatus: input.encryptCsvData ? 'encrypted' : 'plaintext' }),
  unlock: async () => wait({ state: 'ready' as const, hasEncryptedSecrets: true, hasEnvironmentCredentials: false, csvEncryptionStatus: 'plaintext' as const }),
  lock: async () => wait({ state: 'locked' as const, hasEncryptedSecrets: true, hasEnvironmentCredentials: false, csvEncryptionStatus: 'plaintext' as const }),
  createLinkToken: async () => wait({ linkToken: 'mock-link-token' }),
  exchangePublicToken: async () => wait(mockAccounts),
  sync: async (): Promise<SyncResult> => wait({ syncId: 'mock-sync', status: 'COMPLETED', startedAt: now.toISOString(), finishedAt: now.toISOString(), transactionsAdded: 2, transactionsUpdated: 1, balanceSnapshotsAdded: 3 }),
  getAccounts: async () => wait(mockAccounts),
  getTransactions: async (query: string | TransactionQuery) => wait(matchingTransactions(query)),
  getMonthlyStats: async (month: string) => wait(statsFor(month)),
  getCategoryStats: async (month: string) => wait(categoriesFor(month)),
  getBalanceSnapshots: async () => wait(mockBalances)
};
