import chase from '../assets/institutions/chase.svg';
import bankOfAmerica from '../assets/institutions/boa.svg';
import wellsFargo from '../assets/institutions/wells.svg';
import citi from '../assets/institutions/citi.svg';
import americanExpress from '../assets/institutions/american-express.svg';
import { createInstitutionResolver, type InstitutionBrand } from './institutionResolver';

// Add provider IDs only with verified provenance. Names support legacy connections.
export const institutionBrands: readonly InstitutionBrand[] = [
  { key: 'chase', displayName: 'Chase', aliases: ['Chase Bank', 'JPMorgan Chase', 'JPMorgan Chase Bank', 'JPMorgan Chase Bank, N.A.'], plaidInstitutionIds: [], logoSrc: chase },
  { key: 'bank-of-america', displayName: 'Bank of America', aliases: ['Bank of America, N.A.'], plaidInstitutionIds: [], logoSrc: bankOfAmerica },
  { key: 'wells-fargo', displayName: 'Wells Fargo', aliases: ['Wells Fargo Bank', 'Wells Fargo Bank, N.A.'], plaidInstitutionIds: [], logoSrc: wellsFargo },
  { key: 'citi', displayName: 'Citi', aliases: ['Citibank', 'Citibank Online', 'Citibank, N.A.', 'Citi Cards'], plaidInstitutionIds: [], logoSrc: citi },
  { key: 'american-express', displayName: 'American Express', aliases: ['Amex', 'American Express Bank', 'American Express National Bank'], plaidInstitutionIds: [], logoSrc: americanExpress }
];

export const resolveInstitutionBrand = createInstitutionResolver(institutionBrands);
