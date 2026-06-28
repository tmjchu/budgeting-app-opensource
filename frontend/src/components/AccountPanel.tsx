import type { Account } from '../lib/types';

type Props = {
  accounts: Account[];
};

export function AccountPanel({ accounts }: Props) {
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>Accounts</h2>
        <span>{accounts.length}</span>
      </div>
      {accounts.length === 0 ? (
        <p className="empty-state">No accounts connected.</p>
      ) : (
        <div className="stack">
          {accounts.map((account) => (
            <div className="account-row" key={account.accountId}>
              <div>
                <strong>{account.name}</strong>
                <p>
                  {account.subtype ?? account.type ?? 'Account'}
                  {account.mask ? ` • ${account.mask}` : ''}
                </p>
              </div>
              <span className={account.tracked ? 'status active' : 'status'}>{account.tracked ? 'Tracked' : 'Off'}</span>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
