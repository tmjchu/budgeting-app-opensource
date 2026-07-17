import { useCallback, useEffect, useMemo, useState } from 'react';
import { usePlaidLink } from './hooks/usePlaidLink';
import { api } from './lib/api';
import { formatCurrency, formatDateTime } from './lib/format';
import type {
  Account,
  BalanceSnapshot,
  CategoryStats,
  MonthlyStats,
  SyncResult,
  Transaction,
  TransactionQuery
} from './lib/types';

type View = 'dashboard' | 'spending' | 'transactions' | 'accounts';
type AmountFilter = 'all' | 'spending' | 'income' | 'excluded' | 'pending';

const CATEGORY_COLORS = [
  '#e33bb7',
  '#f17c00',
  '#3f6df6',
  '#248f57',
  '#23a6a6',
  '#8b5cf6',
  '#d14d72',
  '#64748b',
  '#c2410c',
  '#0f766e'
];

function currentMonth() {
  return new Date().toISOString().slice(0, 7);
}

function previousMonth(value: string) {
  const date = new Date(`${value}-01T00:00:00`);
  date.setMonth(date.getMonth() - 1);
  return date.toISOString().slice(0, 7);
}

function monthLabel(value: string) {
  return new Intl.DateTimeFormat('en-US', { month: 'long', year: 'numeric' }).format(
    new Date(`${value}-01T00:00:00`)
  );
}

function shortDate(value: string) {
  return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric' }).format(
    new Date(`${value}T00:00:00`)
  );
}

function displayName(transaction: Transaction) {
  return transaction.merchantName ?? transaction.name;
}

function categoryColor(category: string) {
  let hash = 0;
  for (let index = 0; index < category.length; index += 1) {
    hash = category.charCodeAt(index) + ((hash << 5) - hash);
  }
  return CATEGORY_COLORS[Math.abs(hash) % CATEGORY_COLORS.length];
}

function categoryInitial(category: string) {
  return category.replace(/[^a-zA-Z0-9]/g, '').charAt(0).toUpperCase() || '$';
}

function spendTransactions(transactions: Transaction[]) {
  return transactions.filter((transaction) => transaction.amount > 0 && !transaction.excluded);
}

function groupTransactionsByDate(transactions: Transaction[]) {
  return transactions.reduce<Record<string, Transaction[]>>((groups, transaction) => {
    groups[transaction.date] = groups[transaction.date] ?? [];
    groups[transaction.date].push(transaction);
    return groups;
  }, {});
}

function topMerchants(transactions: Transaction[]) {
  const merchants = new Map<string, { name: string; count: number; total: number }>();
  for (const transaction of spendTransactions(transactions)) {
    const name = displayName(transaction);
    const current = merchants.get(name) ?? { name, count: 0, total: 0 };
    current.count += 1;
    current.total += transaction.amount;
    merchants.set(name, current);
  }
  return [...merchants.values()]
    .sort((left, right) => right.count - left.count || right.total - left.total)
    .slice(0, 4);
}

function largestPurchases(transactions: Transaction[]) {
  return spendTransactions(transactions)
    .slice()
    .sort((left, right) => right.amount - left.amount)
    .slice(0, 4);
}

function dailySpend(month: string, transactions: Transaction[]) {
  const [year, monthIndex] = month.split('-').map(Number);
  const days = new Date(year, monthIndex, 0).getDate();
  const totals = Array.from({ length: days }, (_, index) => ({
    day: index + 1,
    amount: 0
  }));
  for (const transaction of spendTransactions(transactions)) {
    if (!transaction.date.startsWith(month)) {
      continue;
    }
    const day = Number(transaction.date.slice(8, 10));
    totals[day - 1].amount += transaction.amount;
  }
  return totals;
}

function latestBalancesByAccount(snapshots: BalanceSnapshot[]) {
  const byAccount = new Map<string, BalanceSnapshot>();
  for (const snapshot of snapshots) {
    const previous = byAccount.get(snapshot.accountId);
    if (!previous || snapshot.syncedAt > previous.syncedAt) {
      byAccount.set(snapshot.accountId, snapshot);
    }
  }
  return [...byAccount.values()].sort((left, right) => left.accountName.localeCompare(right.accountName));
}

function filterTransactions(
  transactions: Transaction[],
  search: string,
  amountFilter: AmountFilter
) {
  const normalizedSearch = search.trim().toLowerCase();
  return transactions.filter((transaction) => {
    const matchesSearch =
      normalizedSearch.length === 0 ||
      displayName(transaction).toLowerCase().includes(normalizedSearch) ||
      transaction.name.toLowerCase().includes(normalizedSearch) ||
      transaction.accountName.toLowerCase().includes(normalizedSearch) ||
      transaction.category.toLowerCase().includes(normalizedSearch);
    if (!matchesSearch) {
      return false;
    }
    if (amountFilter === 'spending') {
      return transaction.amount > 0 && !transaction.excluded;
    }
    if (amountFilter === 'income') {
      return transaction.amount < 0 && !transaction.excluded;
    }
    if (amountFilter === 'excluded') {
      return transaction.excluded;
    }
    if (amountFilter === 'pending') {
      return transaction.pending;
    }
    return true;
  });
}

export function App() {
  const [activeView, setActiveView] = useState<View>('dashboard');
  const [month, setMonth] = useState(currentMonth());
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [explorerTransactions, setExplorerTransactions] = useState<Transaction[]>([]);
  const [monthlyStats, setMonthlyStats] = useState<MonthlyStats | null>(null);
  const [categoryStats, setCategoryStats] = useState<CategoryStats[]>([]);
  const [balanceSnapshots, setBalanceSnapshots] = useState<BalanceSnapshot[]>([]);
  const [lastSync, setLastSync] = useState<SyncResult | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSyncing, setIsSyncing] = useState(false);
  const [isExplorerLoading, setIsExplorerLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [accountFilter, setAccountFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [amountFilter, setAmountFilter] = useState<AmountFilter>('all');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const loadDashboard = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [nextAccounts, nextTransactions, nextStats, nextCategories, nextBalances] = await Promise.all([
        api.getAccounts(),
        api.getTransactions({ month }),
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

  const loadExplorer = useCallback(async () => {
    setIsExplorerLoading(true);
    setError(null);
    try {
      const query: TransactionQuery =
        startDate || endDate ? { startDate, endDate } : { month };
      if (accountFilter) {
        query.accountId = accountFilter;
      }
      if (categoryFilter) {
        query.category = categoryFilter;
      }
      setExplorerTransactions(await api.getTransactions(query));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to load transactions.');
    } finally {
      setIsExplorerLoading(false);
    }
  }, [accountFilter, categoryFilter, endDate, month, startDate]);

  const { connect, isConnecting, error: plaidError } = usePlaidLink(loadDashboard);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  useEffect(() => {
    void loadExplorer();
  }, [loadExplorer]);

  async function syncNow() {
    setIsSyncing(true);
    setError(null);
    try {
      const result = await api.sync();
      setLastSync(result);
      await loadDashboard();
      await loadExplorer();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to sync.');
    } finally {
      setIsSyncing(false);
    }
  }

  const latestBalances = useMemo(() => latestBalancesByAccount(balanceSnapshots), [balanceSnapshots]);
  const filteredExplorerTransactions = useMemo(
    () => filterTransactions(explorerTransactions, search, amountFilter),
    [amountFilter, explorerTransactions, search]
  );
  const categoryOptions = useMemo(
    () =>
      [...new Set([...categoryStats.map((category) => category.category), ...transactions.map((transaction) => transaction.category)])]
        .filter(Boolean)
        .sort((left, right) => left.localeCompare(right)),
    [categoryStats, transactions]
  );
  const activeError = error ?? plaidError;

  return (
    <main className="desktop-app">
      <Sidebar activeView={activeView} onChange={setActiveView} />
      <section className="workspace">
        <Topbar
          activeView={activeView}
          month={month}
          isConnecting={isConnecting}
          isSyncing={isSyncing}
          onMonthChange={setMonth}
          onConnect={connect}
          onSync={syncNow}
        />

        {activeError && <div className="notice error">{activeError}</div>}
        {lastSync && (
          <div className="notice">
            Sync complete: {lastSync.transactionsAdded} added, {lastSync.transactionsUpdated} updated,{' '}
            {lastSync.balanceSnapshotsAdded} balance snapshots.
          </div>
        )}

        {activeView === 'dashboard' && (
          <DashboardView
            month={month}
            stats={monthlyStats}
            transactions={transactions}
            accounts={accounts}
            balances={latestBalances}
            isLoading={isLoading}
          />
        )}
        {activeView === 'spending' && (
          <SpendingView
            month={month}
            stats={monthlyStats}
            categories={categoryStats}
            transactions={transactions}
            onMonthChange={setMonth}
          />
        )}
        {activeView === 'transactions' && (
          <TransactionsView
            accounts={accounts}
            categories={categoryOptions}
            transactions={filteredExplorerTransactions}
            month={month}
            search={search}
            accountFilter={accountFilter}
            categoryFilter={categoryFilter}
            amountFilter={amountFilter}
            startDate={startDate}
            endDate={endDate}
            isLoading={isExplorerLoading}
            onMonthChange={setMonth}
            onSearchChange={setSearch}
            onAccountFilterChange={setAccountFilter}
            onCategoryFilterChange={setCategoryFilter}
            onAmountFilterChange={setAmountFilter}
            onStartDateChange={setStartDate}
            onEndDateChange={setEndDate}
          />
        )}
        {activeView === 'accounts' && (
          <AccountsView accounts={accounts} balances={latestBalances} snapshots={balanceSnapshots} />
        )}
      </section>
    </main>
  );
}

function Sidebar({ activeView, onChange }: { activeView: View; onChange: (view: View) => void }) {
  const items: Array<{ view: View; label: string; mark: string }> = [
    { view: 'dashboard', label: 'Dashboard', mark: 'D' },
    { view: 'spending', label: 'Spending', mark: 'S' },
    { view: 'transactions', label: 'Transactions', mark: 'T' },
    { view: 'accounts', label: 'Accounts', mark: 'A' }
  ];

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">LB</div>
        <div>
          <strong>Local Budget</strong>
          <span>Desktop finance</span>
        </div>
      </div>
      <nav className="nav-list" aria-label="Primary navigation">
        {items.map((item) => (
          <button
            className={activeView === item.view ? 'nav-item active' : 'nav-item'}
            key={item.view}
            onClick={() => onChange(item.view)}
          >
            <span>{item.mark}</span>
            {item.label}
          </button>
        ))}
      </nav>
      <div className="sidebar-footer">
        <p>Local-first data</p>
        <strong>No subscription dashboard for your subscription dashboard.</strong>
      </div>
    </aside>
  );
}

function Topbar({
  activeView,
  month,
  isConnecting,
  isSyncing,
  onMonthChange,
  onConnect,
  onSync
}: {
  activeView: View;
  month: string;
  isConnecting: boolean;
  isSyncing: boolean;
  onMonthChange: (month: string) => void;
  onConnect: () => void;
  onSync: () => void;
}) {
  const title =
    activeView === 'dashboard'
      ? 'Money Dashboard'
      : activeView === 'spending'
        ? 'Spending'
        : activeView === 'transactions'
          ? 'Transactions'
          : 'Accounts';

  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">{monthLabel(month)}</p>
        <h1>{title}</h1>
      </div>
      <div className="topbar-actions">
        <input
          aria-label="Dashboard month"
          className="control month-input"
          type="month"
          value={month}
          onChange={(event) => onMonthChange(event.target.value)}
        />
        <button className="button secondary" onClick={onConnect} disabled={isConnecting || isSyncing}>
          {isConnecting ? 'Connecting...' : 'Connect account'}
        </button>
        <button className="button primary" onClick={onSync} disabled={isSyncing || isConnecting}>
          {isSyncing ? 'Syncing...' : 'Sync now'}
        </button>
      </div>
    </header>
  );
}

function DashboardView({
  month,
  stats,
  transactions,
  accounts,
  balances,
  isLoading
}: {
  month: string;
  stats: MonthlyStats | null;
  transactions: Transaction[];
  accounts: Account[];
  balances: BalanceSnapshot[];
  isLoading: boolean;
}) {
  const recentGroups = groupTransactionsByDate(transactions.slice(0, 8));

  return (
    <div className="view-stack" aria-busy={isLoading}>
      <div className="metric-strip">
        <MetricTile label="Current spend" value={formatCurrency(stats?.spending ?? 0)} tone="spend" />
        <MetricTile label="Income" value={formatCurrency(stats?.income ?? 0)} tone="income" />
        <MetricTile label="Net cash flow" value={formatCurrency(stats?.netCashFlow ?? 0)} tone="cash" />
        <MetricTile label="Transactions" value={new Intl.NumberFormat().format(stats?.transactionCount ?? 0)} />
      </div>

      <div className="dashboard-grid">
        <section className="card hero-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">This month</p>
              <h2>Spending trend</h2>
            </div>
            <strong>{formatCurrency(stats?.spending ?? 0)}</strong>
          </div>
          <TrendChart month={month} transactions={transactions} />
        </section>

        <section className="card">
          <div className="section-heading">
            <h2>Accounts</h2>
            <span>{accounts.length}</span>
          </div>
          <CompactAccountList accounts={accounts} />
        </section>

        <section className="card tall-card">
          <div className="section-heading">
            <h2>Recent transactions</h2>
            <span>{transactions.length}</span>
          </div>
          <GroupedTransactions groups={recentGroups} limit={8} />
        </section>

        <section className="card tall-card">
          <div className="section-heading">
            <h2>Latest balances</h2>
            <span>{balances.length}</span>
          </div>
          <BalanceList balances={balances} />
        </section>
      </div>
    </div>
  );
}

function SpendingView({
  month,
  stats,
  categories,
  transactions,
  onMonthChange
}: {
  month: string;
  stats: MonthlyStats | null;
  categories: CategoryStats[];
  transactions: Transaction[];
  onMonthChange: (month: string) => void;
}) {
  const spend = stats?.spending ?? 0;
  const frequent = topMerchants(transactions);
  const largest = largestPurchases(transactions);

  return (
    <div className="view-stack">
      <div className="segmented-toolbar">
        <button onClick={() => onMonthChange(previousMonth(currentMonth()))}>Last month</button>
        <button className={month === currentMonth() ? 'selected' : ''} onClick={() => onMonthChange(currentMonth())}>
          This month
        </button>
        <input
          aria-label="Custom spending month"
          className="control month-input"
          type="month"
          value={month}
          onChange={(event) => onMonthChange(event.target.value)}
        />
      </div>

      <div className="spending-layout">
        <section className="card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Spending breakdown</p>
              <h2>{monthLabel(month)}</h2>
            </div>
            <strong>{formatCurrency(spend)}</strong>
          </div>
          <div className="breakdown-grid">
            <DonutChart categories={categories} total={spend} />
            <CategoryTable categories={categories} total={spend} />
          </div>
        </section>

        <aside className="insight-column">
          <SummaryCard stats={stats} />
          <InsightList title="Frequent spend" items={frequent} />
          <LargestPurchases transactions={largest} />
        </aside>
      </div>
    </div>
  );
}

function TransactionsView({
  accounts,
  categories,
  transactions,
  month,
  search,
  accountFilter,
  categoryFilter,
  amountFilter,
  startDate,
  endDate,
  isLoading,
  onMonthChange,
  onSearchChange,
  onAccountFilterChange,
  onCategoryFilterChange,
  onAmountFilterChange,
  onStartDateChange,
  onEndDateChange
}: {
  accounts: Account[];
  categories: string[];
  transactions: Transaction[];
  month: string;
  search: string;
  accountFilter: string;
  categoryFilter: string;
  amountFilter: AmountFilter;
  startDate: string;
  endDate: string;
  isLoading: boolean;
  onMonthChange: (month: string) => void;
  onSearchChange: (search: string) => void;
  onAccountFilterChange: (accountId: string) => void;
  onCategoryFilterChange: (category: string) => void;
  onAmountFilterChange: (filter: AmountFilter) => void;
  onStartDateChange: (date: string) => void;
  onEndDateChange: (date: string) => void;
}) {
  return (
    <div className="view-stack" aria-busy={isLoading}>
      <section className="filter-bar">
        <input
          aria-label="Search transactions"
          className="control search-input"
          placeholder="Search merchant, account, or category"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
        />
        <input
          aria-label="Transaction month"
          className="control"
          type="month"
          value={month}
          onChange={(event) => onMonthChange(event.target.value)}
        />
        <input
          aria-label="Start date"
          className="control"
          type="date"
          value={startDate}
          onChange={(event) => onStartDateChange(event.target.value)}
        />
        <input
          aria-label="End date"
          className="control"
          type="date"
          value={endDate}
          onChange={(event) => onEndDateChange(event.target.value)}
        />
        <select
          aria-label="Account filter"
          className="control"
          value={accountFilter}
          onChange={(event) => onAccountFilterChange(event.target.value)}
        >
          <option value="">All accounts</option>
          {accounts.map((account) => (
            <option key={account.accountId} value={account.accountId}>
              {account.name}
            </option>
          ))}
        </select>
        <select
          aria-label="Category filter"
          className="control"
          value={categoryFilter}
          onChange={(event) => onCategoryFilterChange(event.target.value)}
        >
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
        <select
          aria-label="Amount filter"
          className="control"
          value={amountFilter}
          onChange={(event) => onAmountFilterChange(event.target.value as AmountFilter)}
        >
          <option value="all">All amounts</option>
          <option value="spending">Spending</option>
          <option value="income">Income</option>
          <option value="pending">Pending</option>
          <option value="excluded">Excluded</option>
        </select>
      </section>

      <section className="card table-card">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Transaction explorer</p>
            <h2>{transactions.length} results</h2>
          </div>
          {isLoading && <span>Loading</span>}
        </div>
        <TransactionGrid transactions={transactions} />
      </section>
    </div>
  );
}

function AccountsView({
  accounts,
  balances,
  snapshots
}: {
  accounts: Account[];
  balances: BalanceSnapshot[];
  snapshots: BalanceSnapshot[];
}) {
  const snapshotCount = snapshots.length;

  return (
    <div className="accounts-layout">
      <section className="card">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Tracked accounts</p>
            <h2>{accounts.length} accounts</h2>
          </div>
          <span>{snapshotCount} snapshots</span>
        </div>
        <CompactAccountList accounts={accounts} />
      </section>

      <section className="card">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Latest sync</p>
            <h2>Balances</h2>
          </div>
          <span>{balances.length}</span>
        </div>
        <BalanceList balances={balances} />
      </section>
    </div>
  );
}

function MetricTile({ label, value, tone = 'neutral' }: { label: string; value: string; tone?: string }) {
  return (
    <article className={`metric-tile ${tone}`}>
      <p>{label}</p>
      <strong>{value}</strong>
    </article>
  );
}

function CategoryChip({ category }: { category: string }) {
  return (
    <span className="category-chip">
      <span style={{ background: categoryColor(category) }}>{categoryInitial(category)}</span>
      {category}
    </span>
  );
}

function TrendChart({ month, transactions }: { month: string; transactions: Transaction[] }) {
  const points = dailySpend(month, transactions);
  const max = Math.max(...points.map((point) => point.amount), 0);
  const width = 720;
  const height = 220;
  const step = width / Math.max(points.length - 1, 1);
  const path = points
    .map((point, index) => {
      const x = index * step;
      const y = max === 0 ? height - 24 : height - 24 - (point.amount / max) * 160;
      return `${index === 0 ? 'M' : 'L'} ${x.toFixed(2)} ${y.toFixed(2)}`;
    })
    .join(' ');

  return (
    <div className="trend-frame">
      <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Daily spending trend">
        <path className="trend-grid" d="M 0 44 H 720 M 0 92 H 720 M 0 140 H 720 M 0 188 H 720" />
        <path className="trend-area" d={`${path} L ${width} ${height - 24} L 0 ${height - 24} Z`} />
        <path className="trend-line" d={path} />
      </svg>
      <div className="trend-axis">
        <span>1st</span>
        <span>15th</span>
        <span>{points.length}th</span>
      </div>
    </div>
  );
}

function DonutChart({ categories, total }: { categories: CategoryStats[]; total: number }) {
  let offset = 25;
  const radius = 72;
  const circumference = 2 * Math.PI * radius;

  return (
    <div className="donut-wrap">
      <svg viewBox="0 0 200 200" role="img" aria-label="Category spending distribution">
        <circle className="donut-base" cx="100" cy="100" r={radius} />
        {categories.map((category) => {
          const share = total === 0 ? 0 : category.amount / total;
          const dash = share * circumference;
          const circle = (
            <circle
              className="donut-segment"
              cx="100"
              cy="100"
              key={category.category}
              r={radius}
              stroke={categoryColor(category.category)}
              strokeDasharray={`${dash} ${circumference - dash}`}
              strokeDashoffset={offset}
            />
          );
          offset -= dash;
          return circle;
        })}
      </svg>
      <div className="donut-center">
        <span>Total spend</span>
        <strong>{formatCurrency(total)}</strong>
      </div>
    </div>
  );
}

function CategoryTable({ categories, total }: { categories: CategoryStats[]; total: number }) {
  if (categories.length === 0) {
    return <p className="empty-state">No category spending for this month.</p>;
  }

  return (
    <div className="category-table">
      <div className="category-table-head">
        <span>Category</span>
        <span>% Spend</span>
        <span>Transactions</span>
        <span>Amount</span>
      </div>
      {categories.map((category) => {
        const percent = total === 0 ? 0 : Math.round((category.amount / total) * 100);
        return (
          <div className="category-table-row" key={category.category}>
            <CategoryChip category={category.category} />
            <span>{percent}%</span>
            <span>{category.transactionCount}</span>
            <strong>{formatCurrency(category.amount)}</strong>
          </div>
        );
      })}
    </div>
  );
}

function SummaryCard({ stats }: { stats: MonthlyStats | null }) {
  return (
    <section className="card compact-card">
      <div className="section-heading">
        <h2>Summary</h2>
      </div>
      <div className="summary-list">
        <SummaryRow label="Income" value={formatCurrency(stats?.income ?? 0)} tone="positive" />
        <SummaryRow label="Spending" value={formatCurrency(stats?.spending ?? 0)} />
        <SummaryRow label="Net cash flow" value={formatCurrency(stats?.netCashFlow ?? 0)} tone="positive" />
        <SummaryRow label="Transactions" value={new Intl.NumberFormat().format(stats?.transactionCount ?? 0)} />
      </div>
    </section>
  );
}

function SummaryRow({ label, value, tone }: { label: string; value: string; tone?: string }) {
  return (
    <div className={`summary-row ${tone ?? ''}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function InsightList({ title, items }: { title: string; items: Array<{ name: string; count: number; total: number }> }) {
  return (
    <section className="card compact-card">
      <div className="section-heading">
        <h2>{title}</h2>
      </div>
      {items.length === 0 ? (
        <p className="empty-state">No spending yet.</p>
      ) : (
        <div className="list-stack">
          {items.map((item) => (
            <div className="insight-row" key={item.name}>
              <span className="count-pill">{item.count}x</span>
              <div>
                <strong>{item.name}</strong>
                <p>Average {formatCurrency(item.total / item.count)}</p>
              </div>
              <strong>{formatCurrency(item.total)}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function LargestPurchases({ transactions }: { transactions: Transaction[] }) {
  return (
    <section className="card compact-card">
      <div className="section-heading">
        <h2>Largest purchases</h2>
      </div>
      {transactions.length === 0 ? (
        <p className="empty-state">No purchases to show.</p>
      ) : (
        <div className="list-stack">
          {transactions.map((transaction) => (
            <div className="purchase-row" key={transaction.transactionId}>
              <CategoryChip category={transaction.category} />
              <div>
                <strong>{displayName(transaction)}</strong>
                <p>{shortDate(transaction.date)}</p>
              </div>
              <strong>{formatCurrency(transaction.amount)}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function CompactAccountList({ accounts }: { accounts: Account[] }) {
  if (accounts.length === 0) {
    return <p className="empty-state">Connect an account to start tracking local budget data.</p>;
  }

  return (
    <div className="list-stack">
      {accounts.map((account) => (
        <div className="account-line" key={account.accountId}>
          <span className="account-avatar">{account.name.charAt(0).toUpperCase()}</span>
          <div>
            <strong>{account.name}</strong>
            <p>
              {account.subtype ?? account.type ?? 'Account'}
              {account.mask ? ` • ${account.mask}` : ''}
            </p>
          </div>
          <span className={account.tracked ? 'status-pill tracked' : 'status-pill'}>{account.tracked ? 'Tracked' : 'Hidden'}</span>
        </div>
      ))}
    </div>
  );
}

function BalanceList({ balances }: { balances: BalanceSnapshot[] }) {
  if (balances.length === 0) {
    return <p className="empty-state">No balance snapshots yet.</p>;
  }

  return (
    <div className="list-stack">
      {balances.map((balance) => (
        <div className="balance-line" key={balance.snapshotId}>
          <div>
            <strong>{balance.accountName}</strong>
            <p>{formatDateTime(balance.syncedAt)}</p>
          </div>
          <div className="money-block">
            <strong>{formatCurrency(balance.currentBalance ?? 0)}</strong>
            <p>Available {formatCurrency(balance.availableBalance ?? 0)}</p>
          </div>
        </div>
      ))}
    </div>
  );
}

function GroupedTransactions({ groups, limit }: { groups: Record<string, Transaction[]>; limit?: number }) {
  const dates = Object.keys(groups).sort((left, right) => right.localeCompare(left));
  let rendered = 0;

  if (dates.length === 0) {
    return <p className="empty-state">No transactions for this month.</p>;
  }

  return (
    <div className="grouped-list">
      {dates.map((date) => {
        const rows = groups[date].filter(() => {
          if (limit != null && rendered >= limit) {
            return false;
          }
          rendered += 1;
          return true;
        });
        if (rows.length === 0) {
          return null;
        }
        return (
          <div className="date-group" key={date}>
            <div className="date-divider">
              <strong>{shortDate(date)}</strong>
              <span>{formatCurrency(rows.reduce((sum, transaction) => sum + transaction.amount, 0))}</span>
            </div>
            {rows.map((transaction) => (
              <TransactionListRow key={transaction.transactionId} transaction={transaction} />
            ))}
          </div>
        );
      })}
    </div>
  );
}

function TransactionListRow({ transaction }: { transaction: Transaction }) {
  return (
    <div className="transaction-line">
      <CategoryChip category={transaction.category} />
      <div>
        <strong>{displayName(transaction)}</strong>
        <p>{transaction.accountName}</p>
      </div>
      <strong className={transaction.amount < 0 ? 'positive-amount' : ''}>{formatCurrency(transaction.amount)}</strong>
    </div>
  );
}

function TransactionGrid({ transactions }: { transactions: Transaction[] }) {
  if (transactions.length === 0) {
    return <p className="empty-state">No transactions match the current filters.</p>;
  }

  return (
    <div className="data-table">
      <div className="table-header">
        <span>Date</span>
        <span>Name</span>
        <span>Account</span>
        <span>Category</span>
        <span>Status</span>
        <span>Payment</span>
        <span>Amount</span>
      </div>
      {transactions.map((transaction) => (
        <div className="table-row" key={transaction.transactionId}>
          <span>{shortDate(transaction.date)}</span>
          <span>
            <strong>{displayName(transaction)}</strong>
            <p>{transaction.name}</p>
          </span>
          <span>{transaction.accountName}</span>
          <CategoryChip category={transaction.category} />
          <span>{transaction.pending ? 'Pending' : transaction.excluded ? 'Excluded' : 'Posted'}</span>
          <span>{transaction.paymentChannel ?? 'Unknown'}</span>
          <strong className={transaction.amount < 0 ? 'positive-amount amount-cell' : 'amount-cell'}>
            {formatCurrency(transaction.amount)}
          </strong>
        </div>
      ))}
    </div>
  );
}
