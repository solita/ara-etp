import * as R from 'ramda';
import { assert } from 'chai';
import * as objects from './objects';

const assertSameAsValues = object => {
  assert.deepEqual(objects.recursiveValues(R.F, object), R.values(object));
};

describe('Objects:', () => {
  describe('Function recursiveValues:', () => {
    it('Flat objects - same as R.values', () => {
      assertSameAsValues({a: 1});
      assertSameAsValues({a: 1, b: 1});
      assertSameAsValues({a: 1, b: 1, c: 1});
    });

    it('Nested objects', () => {
      assert.deepEqual(objects.recursiveValues(R.F, {a: {b: 1}}), [1]);
      assert.deepEqual(objects.recursiveValues(R.F, {a: {b: {c: 1}}}), [1]);

      assert.deepEqual(objects.recursiveValues(R.F, {a: {b: 1}, c: 1}), [1, 1]);
      assert.deepEqual(objects.recursiveValues(R.F, {a: {b: 1}, c: {d: 1}}), [1, 1]);
    });
  });
});
