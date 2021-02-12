import { assert } from 'chai';
import * as fxmath from './fxmath';

describe('Fixed math:', () => {
  describe('Rounding:', () => {
    it('No rounding', () => {
      assert.equal(fxmath.round(1, 0), 0);
      assert.equal(fxmath.round(1, 1.1), 1.1);
      assert.equal(fxmath.round(1, 1.1), 1.1);
      assert.equal(fxmath.round(1, 1.0), 1.0);
      assert.equal(fxmath.round(1, 2.0), 2.0);
      assert.equal(fxmath.round(1, 10.0), 10.0);
    });

    it('Rounding n.05 (1 decimal)', () => {
      assert.equal(fxmath.round(1, 1.05), 1.1);
      assert.equal(fxmath.round(1, 10.05), 10.1);
      assert.equal(fxmath.round(1, 100.05), 100.1);
      assert.equal(fxmath.round(1, 1000.05), 1000.1);
      assert.equal(fxmath.round(1, 10000.05), 10000.1);
      assert.equal(fxmath.round(1, 100000.05), 100000.1);
    });

    it('Rounding 3456.3456', () => {
      assert.equal(fxmath.round(3, 3456.3456), 3456.346);
      assert.equal(fxmath.round(2, 3456.3456), 3456.35);
      assert.equal(fxmath.round(1, 3456.3456), 3456.3);
      assert.equal(fxmath.round(0, 3456.3456), 3456);
      assert.equal(fxmath.round(-1, 3456.3456), 3460);
      assert.equal(fxmath.round(-2, 3456.3456), 3500);
      assert.equal(fxmath.round(-3, 3456.3456), 3000);
    });

    it('Famous problem cases', () => {
      assert.equal(fxmath.round(2, 1.005), 1.01);
    });

    it('Actual fixed bugs', () => {
      // AE-245
      assert.equal(fxmath.round(1, 71.05), 71.1);
      assert.equal(fxmath.round(1, 203 * 0.35), 71.1);
    });
  });
});
