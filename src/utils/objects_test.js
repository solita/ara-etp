import * as Objects from './objects';
import { assert } from 'chai';

describe('Objects', () => {
  describe('mapKeys', () => {
    it('should map keys instead of values', () => {
      const obj = {
        a: 1,
        b: 2,
        c: 3
      };

      const expected = {
        a_1: 1,
        b_1: 2,
        c_1: 3
      };

      assert.deepEqual(
        Objects.mapKeys(key => `${key}_1`, obj),
        expected
      );
    });
  });

  describe('renameKeys', () => {
    it('should rename keys with given map', () => {
      const keysMap = {
        a: 'z',
        b: 'y',
        c: 'x'
      };

      const obj = {
        a: 1,
        b: 2,
        c: 3
      };

      const expected = {
        z: 1,
        y: 2,
        x: 3
      };

      assert.deepEqual(Objects.renameKeys(keysMap, obj), expected);
    });
  });

  describe('requireNotNil', () => {
    it('should return value when not nil', () => {
      const value = 1;
      assert.equal(Objects.requireNotNil(value, 'err'), value);
    });
    it('should throw when nil', () => {
      const value = null;
      assert.throws(() => Object.requireNotNil(value, 'err'));
    });
  });
});
