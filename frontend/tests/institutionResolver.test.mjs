import test from 'node:test';
import assert from 'node:assert/strict';
import { createInstitutionResolver, accountIconKind } from '../src/lib/institutionResolver.ts';

const chase = { key: 'chase', displayName: 'Chase', aliases: ['JPMorgan Chase Bank, N.A.'], plaidInstitutionIds: ['test-chase', 'test-chase-oauth'], logoSrc: '/chase.svg' };
const citi = { key: 'citi', displayName: 'Citi', aliases: ['Citibank'], plaidInstitutionIds: ['test-citi'], logoSrc: '/citi.svg' };
const resolve = createInstitutionResolver([chase, citi]);

test('matches exact normalized institution names, including Unicode and punctuation', () => {
  assert.equal(resolve({ institutionName: '  ＪＰＭｏｒｇａｎ   Chase Bank, N.A.  ' }), chase);
  assert.equal(resolve({ institutionName: 'CITIBANK' }), citi);
});
test('verified ID wins conflicts and multiple IDs map to the same issuer', () => {
  for (const institutionId of chase.plaidInstitutionIds) {
    assert.equal(resolve({ institutionId, institutionName: 'Citi' }), chase);
  }
  assert.equal(resolve({ institutionId: 'unknown', institutionName: 'Citi' }), citi);
});
test('never guesses issuers from nicknames or partial/unknown institution names', () => {
  for (const institutionName of [null, '', 'Chase Community Credit Union', 'My Citi Savings']) {
    assert.equal(resolve({ institutionName, name: 'Chase' }), undefined);
  }
  assert.equal(resolve({ institutionName: 'Citi', name: 'Renamed account' }), citi);
});
test('rejects ambiguous normalized aliases and provider IDs', () => {
  assert.throws(() => createInstitutionResolver([chase, { ...citi, aliases: ['Chase'] }]), /Duplicate/);
  assert.throws(() => createInstitutionResolver([chase, { ...citi, plaidInstitutionIds: ['test-chase'] }]), /Duplicate/);
});
test('fallback distinguishes deposit, credit, and other accounts', () => {
  assert.equal(accountIconKind('credit'), 'card');
  assert.equal(accountIconKind(null, 'Credit Card'), 'card');
  assert.equal(accountIconKind('depository'), 'bank');
  assert.equal(accountIconKind(undefined, 'savings'), 'bank');
  assert.equal(accountIconKind('investment'), 'wallet');
  assert.equal(accountIconKind(), 'wallet');
});
