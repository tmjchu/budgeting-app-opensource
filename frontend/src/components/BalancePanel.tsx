import type { BalanceSnapshot } from '../lib/types';
import { formatCurrency, formatDateTime } from '../lib/format';

type Props = {
  snapshots: BalanceSnapshot[];
};

export function BalancePanel({ snapshots }: Props) {
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>Balances</h2>
        <span>{snapshots.length}</span>
      </div>
      {snapshots.length === 0 ? (
        <p className="empty-state">Sync to capture balance snapshots.</p>
      ) : (
        <div className="stack">
          {snapshots.map((snapshot) => (
            <div className="balance-row" key={snapshot.snapshotId}>
              <div>
                <strong>{snapshot.accountName}</strong>
                <p>{formatDateTime(snapshot.syncedAt)}</p>
              </div>
              <div className="money-block">
                <strong>{formatCurrency(snapshot.currentBalance ?? 0)}</strong>
                <p>Available {formatCurrency(snapshot.availableBalance ?? 0)}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
