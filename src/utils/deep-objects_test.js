import * as R from 'ramda';
import { assert } from 'chai';
import * as objects from './deep-objects';

const assertSameAsValues = object => {
  assert.deepEqual(objects.values(R.F, object), R.values(object));
};

const assertSameAsMap = (fn, object) => {
  assert.deepEqual(objects.map(R.F, fn, object), R.map(fn, object));
};

const assertSameAsMergeRight = (left, right) => {
  assert.deepEqual(objects.mergeRight(R.F, left, right), R.mergeRight(left, right));
};

describe('Deep objects:', () => {
  describe('Deep values:', () => {
    it('Flat objects - same as R.values', () => {
      assertSameAsValues({a: 1});
      assertSameAsValues({a: 1, b: 1});
      assertSameAsValues({a: 1, b: 1, c: 1});
    });

    it('Nested objects', () => {
      assert.deepEqual(objects.values(R.F, {a: {b: 1}}), [1]);
      assert.deepEqual(objects.values(R.F, {a: {b: {c: 1}}}), [1]);

      assert.deepEqual(objects.values(R.F, {a: {b: 1}, c: 1}), [1, 1]);
      assert.deepEqual(objects.values(R.F, {a: {b: 1}, c: {d: 1}}), [1, 1]);
    });
  });

  describe('Deep map:', () => {
    it('Flat objects - same as R.map', () => {
      assertSameAsMap(a => a + 1, {a: 1});
      assertSameAsMap(a => a + 1, {a: 1, b: 1});
      assertSameAsMap(a => a + 1, {a: 1, b: 1, c: 1});
    });

    it('Nested objects', () => {
      assert.deepEqual(objects.map(R.F, a => a + 1, {a: {b: 1}}), {a: {b: 2}});
      assert.deepEqual(objects.map(R.F, a => a + 1, {a: {b: {c: 1}}}), {a: {b: {c: 2}}});
    });
  });

  describe('Deep mergeRight:', () => {
    it('Flat objects - same as R.mergeRight', () => {
      assertSameAsMergeRight({a: 1}, {b: 1})
      assertSameAsMergeRight({a: 1}, {a: 2})
      assertSameAsMergeRight({a: 1}, {})
    });

    it('Nested objects', () => {
      assert.deepEqual(objects.mergeRight(R.F, {a: {b: 1}}, {a: {b: 2}}), {a: {b: 2}});
    });

    it('Arrays', () => {
      assert.deepEqual(objects.mergeRight(R.F, [1], [2]), [2]);
      assert.deepEqual(objects.mergeRight(R.F, {a: [1, 2]}, {a: [2]}), {a: [2, 2]});
    });

    it('Mixed', () => {
      assert.deepEqual(objects.mergeRight(R.F, [1], {a: 2}), {"0": 1, a: 2});
    });
  });
});
