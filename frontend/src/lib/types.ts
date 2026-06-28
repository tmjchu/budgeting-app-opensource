export type Account = {
  accountId: string;
  name: string;
  mask?: string;
  type?: string;
  subtype?: string;
  tracked: boolean;
};

export type BalanceSnapshot = {
  snapshotId: string;
  syncedAt: string;
  accountId: string;
  accountName: string;
  accountMask?: string;
  currentBalance: number | null;
  availableBalance: number | null;
  isoCurrencyCode?: string;
};

export type CategoryStats = {
  category: string;
  amount: number;
  transactionCount: number;
};

export type MonthlyStats = {
  month: string;
  income: number;
  spending: number;
  netCashFlow: number;
  transactionCount: number;
};

export type SyncResult = {
  syncId: string;
  status: string;
  startedAt: string;
  finishedAt?: string;
  transactionsAdded: number;
  transactionsUpdated: number;
  balanceSnapshotsAdded: number;
  errorMessage?: string;
};

export type Transaction = {
  transactionId: string;
  accountId: string;
  accountName: string;
  date: string;
  name: string;
  merchantName?: string;
  amount: number;
  category: string;
  pending: boolean;
  excluded: boolean;
  paymentChannel?: string;
};
