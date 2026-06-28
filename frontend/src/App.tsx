import { useCallback, useEffect, useMemo, useState } from 'react';
import { AccountPanel } from './components/AccountPanel';
import { BalancePanel } from './components/BalancePanel';
import { CategoryBreakdown } from './components/CategoryBreakdown';
import { MetricCard } from './components/MetricCard';
import { TransactionTable } from './components/TransactionTable';
import { usePlaidLink } from './hooks/usePlaidLink';
import { api } from './lib/api';
import type {
  Account,
  BalanceSnapshot,
  CategoryStats,
  MonthlyStats,
  SyncResult,
  Transaction
} from './lib/types';

function currentMonth() {
  return new Date().toISOString().slice(0, 7);
}

export function App() {
  const [month, setMonth] = useState(currentMonth());
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [monthlyStats, setMonthlyStats] = useState<MonthlyStats | null>(null);
  const [categoryStats, setCategoryStats] = useState<CategoryStats[]>([]);
  const [balanceSnapshots, setBalanceSnapshots] = useState<BalanceSnapshot[]>([]);
  const [lastSync, setLastSync] = useState<SyncResult | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSyncing, setIsSyncing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [nextAccounts, nextTransactions, nextStats, nextCategories, nextBalances] = await Promise.all([
        api.getAccounts(),
        api.getTransactions(month),
        api.getMonthlyStats(month),
        api.getCategoryStats(month),
        api.getBalanceSnapshots()
      ]);
      setAccounts(nextAccounts);
      setTransactions(nextTransactions);
      setMonthlyStats(nextStats);
      setCategoryStats(nextCategories);
      setBalanceSnapshots(nextBalances);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to load dashboard.');
    } finally {
      setIsLoading(false);
    }
  }, [month]);

  const { connect, isConnecting, error: plaidError } = usePlaidLink(loadDashboard);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  async function syncNow() {
    setIsSyncing(true);
    setError(null);
    try {
      const result = await api.sync();
      setLastSync(result);
      await loadDashboard();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to sync.');
    } finally {
      setIsSyncing(false);
    }
  }

  const latestBalances = useMemo(() => {
    const byAccount = new Map<string, BalanceSnapshot>();
    for (const snapshot of balanceSnapshots) {
      const previous = byAccount.get(snapshot.accountId);
      if (!previous || snapshot.syncedAt > previous.syncedAt) {
        byAccount.set(snapshot.accountId, snapshot);
      }
    }
    return [...byAccount.values()].sort((left, right) => left.accountName.localeCompare(right.accountName));
  }, [balanceSnapshots]);

  const activeError = error ?? plaidError;

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Local Budget</p>
          <h1>Money Dashboard</h1>
        </div>
        <div className="topbar-actions">
          <input
            aria-label="Dashboard month"
            className="month-input"
            type="month"
            value={month}
            onChange={(event) => setMonth(event.target.value)}
          />
          <button className="button secondary" onClick={connect} disabled={isConnecting || isSyncing}>
            {isConnecting ? 'Connecting...' : 'Connect account'}
          </button>
          <button className="button primary" onClick={syncNow} disabled={isSyncing || isConnecting}>
            {isSyncing ? 'Syncing...' : 'Sync now'}
          </button>
        </div>
      </header>

      {activeError && <div className="notice error">{activeError}</div>}
      {lastSync && (
        <div className="notice">
          Sync complete: {lastSync.transactionsAdded} added, {lastSync.transactionsUpdated} updated,{' '}
          {lastSync.balanceSnapshotsAdded} balance snapshots.
        </div>
      )}

      <section className="metrics-grid" aria-busy={isLoading}>
        <MetricCard label="Income" value={monthlyStats?.income ?? 0} tone="positive" />
        <MetricCard label="Spending" value={monthlyStats?.spending ?? 0} tone="negative" />
        <MetricCard label="Net cash flow" value={monthlyStats?.netCashFlow ?? 0} tone="neutral" />
        <MetricCard label="Transactions" value={monthlyStats?.transactionCount ?? 0} />
      </section>

      <section className="content-grid">
        <AccountPanel accounts={accounts} />
        <BalancePanel snapshots={latestBalances} />
        <CategoryBreakdown categories={categoryStats} />
      </section>

      <TransactionTable transactions={transactions} />

      <footer className="app-credit">
        Created by Tommy.
      </footer>
    </main>
  );
}
