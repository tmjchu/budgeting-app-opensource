import type { Transaction } from '../lib/types';
import { formatCurrency } from '../lib/format';

type Props = {
  transactions: Transaction[];
};

export function TransactionTable({ transactions }: Props) {
  return (
    <section className="table-panel">
      <div className="panel-heading">
        <h2>Transactions</h2>
        <span>{transactions.length}</span>
      </div>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Name</th>
              <th>Account</th>
              <th>Category</th>
              <th>Status</th>
              <th className="amount-column">Amount</th>
            </tr>
          </thead>
          <tbody>
            {transactions.length === 0 ? (
              <tr>
                <td colSpan={6} className="empty-row">
                  No transactions for this month.
                </td>
              </tr>
            ) : (
              transactions.map((transaction) => (
                <tr key={transaction.transactionId}>
                  <td>{transaction.date}</td>
                  <td>
                    <strong>{transaction.merchantName ?? transaction.name}</strong>
                    <p>{transaction.name}</p>
                  </td>
                  <td>{transaction.accountName}</td>
                  <td>{transaction.category}</td>
                  <td>{transaction.pending ? 'Pending' : transaction.excluded ? 'Excluded' : 'Posted'}</td>
                  <td className="amount-column">{formatCurrency(transaction.amount)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
