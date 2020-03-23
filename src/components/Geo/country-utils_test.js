import { assert } from 'chai';
import * as country from './country-utils';
import * as Maybe from '@Utility/maybe-utils';

const testFindCountry = (keyword, countries, expectedCountry) => {
  assert.isTrue(
    Maybe.Some(countries[expectedCountry]).equals(
      country.findCountry(keyword, countries)
    )
  );
};

describe('Country related functions:', () => {
  describe('findCountry', () => {
    it('Positive tests', () => {
      testFindCountry('asdf', [{ id: 'FI', 'label-fi': 'asdf' }], 0);
      testFindCountry('FI', [{ id: 'FI', 'label-fi': 'asdf' }], 0);
    });
  });
});
