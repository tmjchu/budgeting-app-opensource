import type { CategoryStats } from '../lib/types';
import { formatCurrency } from '../lib/format';

type Props = {
  categories: CategoryStats[];
};

export function CategoryBreakdown({ categories }: Props) {
  const max = Math.max(...categories.map((category) => category.amount), 0);

  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>Categories</h2>
        <span>{categories.length}</span>
      </div>
      {categories.length === 0 ? (
        <p className="empty-state">No spending for this month.</p>
      ) : (
        <div className="category-list">
          {categories.map((category) => {
            const width = max === 0 ? 0 : Math.max(8, (category.amount / max) * 100);
            return (
              <div className="category-row" key={category.category}>
                <div className="category-label">
                  <span>{formatLabel(category.category)}</span>
                  <strong>{formatCurrency(category.amount)}</strong>
                </div>
                <div className="bar-track">
                  <div className="bar-fill" style={{ width: `${width}%` }} />
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}
