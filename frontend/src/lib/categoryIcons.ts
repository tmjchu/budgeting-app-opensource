const icons = import.meta.glob<string>('../assets/categories/*.svg', {
  eager: true,
  query: '?url',
  import: 'default'
});

const aliases: Record<string, string> = {
  transportation: 'auto-transport', utilities: 'bills-utilities', dining: 'dining-drinks',
  entertainment: 'entertainment-rec', housing: 'home-garden', transfer: 'internal-transfers',
  'food-and-drink': 'dining-drinks', 'rent-and-utilities': 'bills-utilities',
  'general-merchandise': 'shopping', 'transfer-in': 'internal-transfers',
  'transfer-out': 'internal-transfers', travel: 'travel-vacation'
};

export function categoryIcon(category: string): string {
  const key = category.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
  return icons[`../assets/categories/${aliases[key] ?? key}.svg`]
    ?? icons['../assets/categories/uncategorized.svg'];
}
