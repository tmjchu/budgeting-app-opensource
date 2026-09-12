import goldmanSachs from '../assets/institutions/goldman-sachs.svg';
import usBank from '../assets/institutions/us-bank.svg';
import capitalOne from '../assets/institutions/capital-one.svg';
import pnc from '../assets/institutions/pnc.svg';
import truist from '../assets/institutions/truist.svg';
import bny from '../assets/institutions/bny.svg';
import bilt from '../assets/institutions/bilt.svg';
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
  { key: 'american-express', displayName: 'American Express', aliases: ['Amex', 'American Express Bank', 'American Express National Bank'], plaidInstitutionIds: [], logoSrc: americanExpress },
  { key: "goldman-sachs", displayName: "Goldman Sachs", aliases: ["Goldman Sachs Bank USA", "Marcus", "Marcus by Goldman Sachs"], plaidInstitutionIds: [], logoSrc: goldmanSachs },
  { key: "us-bank", displayName: "U.S. Bank", aliases: ["US Bank", "U S Bank", "U.S. Bank National Association", "U.S. Bancorp"], plaidInstitutionIds: [], logoSrc: usBank },
  { key: "capital-one", displayName: "Capital One", aliases: ["Capital One Bank", "Capital One, N.A.", "Capital One 360"], plaidInstitutionIds: [], logoSrc: capitalOne },
  { key: "pnc", displayName: "PNC", aliases: ["PNC Bank", "PNC Bank, N.A.", "PNC Bank, National Association"], plaidInstitutionIds: [], logoSrc: pnc },
  { key: "truist", displayName: "Truist", aliases: ["Truist Bank", "Truist Financial"], plaidInstitutionIds: [], logoSrc: truist },
  { key: "bny", displayName: "BNY", aliases: ["BNY Mellon", "Bank of New York Mellon", "The Bank of New York Mellon"], plaidInstitutionIds: [], logoSrc: bny },
  { key: "bilt", displayName: "Bilt Rewards", aliases: ["Bilt"], plaidInstitutionIds: [], logoSrc: bilt }
];

export const resolveInstitutionBrand = createInstitutionResolver(institutionBrands);
