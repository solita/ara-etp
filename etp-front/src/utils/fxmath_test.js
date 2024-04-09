import { expect, describe, it } from '@jest/globals';
import * as fxmath from './fxmath';

describe('Fixed math:', () => {
  describe('Rounding:', () => {
    it('No rounding', () => {
      expect(fxmath.round(1, 0)).toEqual(0);
      expect(fxmath.round(1, 1.1)).toEqual(1.1);
      expect(fxmath.round(1, 1.1)).toEqual(1.1);
      expect(fxmath.round(1, 1.0)).toEqual(1.0);
      expect(fxmath.round(1, 2.0)).toEqual(2.0);
      expect(fxmath.round(1, 10.0)).toEqual(10.0);
    });

    it('Rounding n.05 (1 decimal)', () => {
      expect(fxmath.round(1, 1.05)).toEqual(1.1);
      expect(fxmath.round(1, 10.05)).toEqual(10.1);
      expect(fxmath.round(1, 100.05)).toEqual(100.1);
      expect(fxmath.round(1, 1000.05)).toEqual(1000.1);
      expect(fxmath.round(1, 10000.05)).toEqual(10000.1);
      expect(fxmath.round(1, 100000.05)).toEqual(100000.1);
    });

    it('Rounding 3456.3456', () => {
      expect(fxmath.round(3, 3456.3456)).toEqual(3456.346);
      expect(fxmath.round(2, 3456.3456)).toEqual(3456.35);
      expect(fxmath.round(1, 3456.3456)).toEqual(3456.3);
      expect(fxmath.round(0, 3456.3456)).toEqual(3456);
      expect(fxmath.round(-1, 3456.3456)).toEqual(3460);
      expect(fxmath.round(-2, 3456.3456)).toEqual(3500);
      expect(fxmath.round(-3, 3456.3456)).toEqual(3000);
    });

    it('Famous problem cases', () => {
      expect(fxmath.round(2, 1.005)).toEqual(1.01);
    });

    it('Actual fixed bugs', () => {
      // AE-245
      expect(fxmath.round(1, 71.05)).toEqual(71.1);
      expect(fxmath.round(1, 203 * 0.35)).toEqual(71.1);
    });
  });
});
