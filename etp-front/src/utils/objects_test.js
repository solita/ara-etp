import { expect, describe, it } from '@jest/globals';
import * as Objects from './objects';

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

      expect(Objects.mapKeys(key => `${key}_1`, obj)).toEqual(expected);
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

      expect(Objects.renameKeys(keysMap, obj)).toEqual(expected);
    });
  });

  describe('requireNotNil', () => {
    it('should return value when not nil', () => {
      const value = 1;
      expect(Objects.requireNotNil(value, 'err')).toEqual(value);
    });
    it('should throw when nil', () => {
      const value = null;
      expect(() => Object.requireNotNil(value, 'err')).toThrow();
    });
  });
});
