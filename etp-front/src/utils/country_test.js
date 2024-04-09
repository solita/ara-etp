import { expect, describe, it } from '@jest/globals';
import * as country from './country';
import * as Maybe from '@Utility/maybe-utils';

const testFindCountry = (keyword, countries, expectedCountry) => {
  expect(
    Maybe.Some(countries[expectedCountry]).equals(
      country.findCountry(keyword, countries)
    )
  ).toBe(true);
};

describe('Country related functions:', () => {
  describe('findCountry', () => {
    it('Positive tests', () => {
      testFindCountry(
        'asdf',
        [{ id: 'FI', 'label-fi': 'asdf', 'label-sv': 'asdf' }],
        0
      );
      testFindCountry(
        'FI',
        [{ id: 'FI', 'label-fi': 'asdf', 'label-sv': 'asdf' }],
        0
      );
    });
  });
});
