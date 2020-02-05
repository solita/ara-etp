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

    it('should return false with value lower than low', () => {
      const low = 1;
      const high = 2;
      const value = 0;

      const expected = false;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).to.eql(expected);
    });

    it('should return false with value higher than high', () => {
      const low = 0;
      const high = 1;
      const value = 2;

      const expected = false;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).to.eql(expected);
    });

    it('should return true with high edge', () => {
      const low = 0;
      const high = 1;
      const value = 1;

      const expected = true;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).to.eql(expected);
    });

    it('should return true with low edge', () => {
      const low = 0;
      const high = 1;
      const value = 0;

      const expected = true;
      expect(RamdaUtils.inRangeInclusive(low, high, value)).to.eql(expected);
    });
  });
});
