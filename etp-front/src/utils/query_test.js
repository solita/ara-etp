import { expect, describe, it } from '@jest/globals';
import * as Maybe from './maybe-utils';
import * as Query from './query';

describe('Query', () => {
  it('should return querystring with only some values', () => {
    const queryObject = {
      'should-be-included': Maybe.Some(1),
      'should-also-be-included': Maybe.Some(2),
      'should-not-be-included': Maybe.None()
    };

    const expected = '?should-be-included=1&should-also-be-included=2';

    expect(Query.toQueryString(queryObject)).toEqual(expected);
  });

  it('should return empty with all values none', () => {
    const queryObject = {
      'should-not-be-included': Maybe.None()
    };
    const expected = '';

    expect(Query.toQueryString(queryObject)).toEqual(expected);
  });
});
