import { formatCurrency } from '../lib/format';

type Props = {
  label: string;
  value: number;
  tone?: 'positive' | 'negative' | 'neutral';
};

export function MetricCard({ label, value, tone = 'neutral' }: Props) {
  const display = label === 'Transactions' ? new Intl.NumberFormat().format(value) : formatCurrency(value);

  return (
    <article className={`metric-card ${tone}`}>
      <p>{label}</p>
      <strong>{display}</strong>
    </article>
  );
}
