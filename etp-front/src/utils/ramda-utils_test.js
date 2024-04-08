import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';

import * as RamdaUtils from './ramda-utils';
import * as Maybe from './maybe-utils';

describe('RamdaUtils:', () => {
  describe('inRangeInclusive', () => {
    it('should return true with value in range', () => {
      const low = 0;
      const high = 2;
      const value = 1;

      const expected = true;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).toBe(true);
    });

    it('should return false with value lower than low', () => {
      const low = 1;
      const high = 2;
      const value = 0;

      const expected = false;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).toBe(false);
    });

    it('should return false with value higher than high', () => {
      const low = 0;
      const high = 1;
      const value = 2;

      const expected = false;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).toBe(false);
    });

    it('should return true with high edge', () => {
      const low = 0;
      const high = 1;
      const value = 1;

      const expected = true;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).toBe(true);
    });

    it('should return true with low edge', () => {
      const low = 0;
      const high = 1;
      const value = 0;

      const expected = true;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).toBe(true);
    });
  });

  describe('fillAndTake', () => {
    it('should fill given array and take fill amount', () => {
      const arr = R.map(Maybe.Some, [1, 2, 3]);
      const expected = [...arr, Maybe.None(), Maybe.None(), Maybe.None()];

      expect(RamdaUtils.fillAndTake(6, Maybe.None, arr)).toEqual(expected);
    });
  });
});
