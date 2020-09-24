import * as R from 'ramda';

/**
 * This library contains implementations of some rambda functionality
 * for nested object structures.
 */

export const isObject = R.is(Object);
export const isArray = R.is(Array);

export const values = R.curry((isValue, object) => R.compose(
  R.flatten,
  R.map(R.ifElse(R.allPass([isObject, R.complement(isValue)]),
        object => values(isValue, object),
        R.identity)),
  R.values)(object));

export const map = R.curry((isValue, mapping, functor) =>
  R.map(R.ifElse(R.allPass([isObject, R.complement(isValue)]),
    object => map(isValue, mapping, object), mapping),
    functor));

export const filter = R.curry((isValue, predicate, filterable) =>
  R.compose(
    R.filter(R.ifElse(R.allPass([isObject, R.complement(isValue)]), R.T, predicate)),
    R.map(
      R.ifElse(R.allPass([isObject, R.complement(isValue)]),
          f => filter(isValue, predicate, f),
        R.identity)))(filterable));

const toArray = object => {
  const result = Array(R.length(R.keys(object)))
  R.forEachObjIndexed((value, key) => {
    result[parseInt(key)] = value;
  }, object);
  return result;
}

export const mergeRight = R.curry((isValue, left, right) =>
  R.compose(
    R.when(R.always(isArray(left) && isArray(right)), toArray),
    R.mergeWith(R.compose(
      R.ifElse(
        R.all(R.allPass([isObject, R.complement(isValue)])),
        ([l, r]) => mergeRight(isValue, l, r),
        R.nth(1)),
      R.pair)))(left, right));

const addPrefix = (prefix, separator, key) =>
  R.isNil(prefix) ? key : prefix + separator + key;

const treeFlatPrefixed = R.curry((prefix, separator, isValue, object) =>
  R.reduce(
    (result, [key, value]) => R.is(Object, value) && !isValue(value) ?
          R.mergeRight(result, treeFlatPrefixed(addPrefix(prefix, separator, key),
            separator, isValue, value)) :
          R.assoc(addPrefix(prefix, separator, key), value, result),
    {}, R.toPairs(object)));

export const treeFlat = treeFlatPrefixed(null);