import { expect } from 'chai';
import * as RamdaUtils from './ramda-utils';

describe('RamdaUtils:', () => {
  describe('inRangeInclusive', () => {
    it('should return true with value in range', () => {
      const low = 0;
      const high = 2;
      const value = 1;

      const expected = true;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).to.eql(expected);
    });
  });
});
