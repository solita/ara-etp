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

const assertSameAsIdentity = (fn, object) => {
  assert.deepEqual(fn(object), object);
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

    it('Value objects', () => {
      const isValue = R.equals({a: 1});
      assert.deepEqual(objects.values(isValue, {a: {a: 1}}), [{a: 1}]);
      assert.deepEqual(objects.values(isValue, {a: {a: {a: 1}}}), [{a: 1}]);
      assert.deepEqual(objects.values(isValue, {a: {a: {a: {a: 1}}}}), [{a: 1}]);
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
      assert.deepEqual(objects.mergeRight(R.F, {a: [2, 2]}, {a: [1]}), {a: [1, 2]});
    });

    it('Mixed', () => {
      assert.deepEqual(objects.mergeRight(R.F, [1], {a: 2}), {"0": 1, a: 2});
    });
  });

  describe('TreeFlat:', () => {
    it('Flat objects - no change', () => {
      assertSameAsIdentity(objects.treeFlat("", R.F), {});
      assertSameAsIdentity(objects.treeFlat("a", R.F),{a: 1})
      assertSameAsIdentity(objects.treeFlat("b", R.F),{a: 1, b: 1})
    });

    it('Nested objects', () => {
      assert.deepEqual(objects.treeFlat("_", R.F, {a: {b: 1}}), {a_b: 1});
      assert.deepEqual(objects.treeFlat("_", R.F, {a: {b: {c: 1}}}), {a_b_c: 1});

      assert.deepEqual(objects.treeFlat("_", R.F, {a: {c: 1}, b: {c: 1}}), {a_c: 1, b_c: 1});
    });

    it('Nested objects with arrays', () => {
      assert.deepEqual(objects.treeFlat("_", R.F, {a: [1]}), {a_0: 1});
      assert.deepEqual(objects.treeFlat("_", R.F, {a: {b: [1]}}), {a_b_0: 1});
    });
  });

  describe('Filter:', () => {
    it('Flat objects - no filtering', () => {
      assertSameAsIdentity(objects.filter(R.F, R.T), {});
      assertSameAsIdentity(objects.filter(R.F, R.T),{a: 1})
      assertSameAsIdentity(objects.filter(R.F, R.T),{a: 1, b: 1})
      assertSameAsIdentity(objects.filter(R.F, R.T),{a: 1, b: 1, c: 1})
    });

    it('Nested objects - no filtering', () => {
      assertSameAsIdentity(objects.filter(R.F, R.T), {a: {b: 1}});
      assertSameAsIdentity(objects.filter(R.F, R.T),{a: {b: {c: 1}}})
      assertSameAsIdentity(objects.filter(R.F, R.T),{a: {c: 1}, b: {c: 1}})
    });

    it('Nested objects with arrays - no filtering', () => {
      assertSameAsIdentity(objects.filter(R.F, R.T), {a: [1]});
      assertSameAsIdentity(objects.filter(R.F, R.T),{a: {b: [1]}});
    });

    it('Flat objects - filter nulls', () => {
      assert.deepEqual(objects.filter(R.F, R.complement(R.isNil), {a: 1, b: null}), {a: 1});
      assert.deepEqual(objects.filter(R.F, R.complement(R.isNil), {a: 1, b: null, c: null}), {a: 1});
    });

    it('Nested objects - filter nulls', () => {
      assert.deepEqual(objects.filter(R.F, R.complement(R.isNil), {a: {b: 1, x: null}}), {a: {b: 1}});
      assert.deepEqual(objects.filter(R.F, R.complement(R.isNil), {a: {b: {c: 1, x: null}}}), {a: {b: {c: 1}}});
    });
  });
});
