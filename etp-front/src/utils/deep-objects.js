/**
 * @module DeepObjects
 * @description This library contains implementations of some ramda functionality for nested object structures.
 */

import * as R from 'ramda';

/**
 * @sig a -> boolean
 * @description Tests if given input is an object
 * @example
 * isObject({a: 1})  // true
 * isObject([1,2,3]) // true
 * isObject(1)       // false
 * isObject('test')  // false
 */
export const isObject = R.is(Object);

/**
 * @sig a -> boolean
 * @description Tests if given input is an array
 * @example
 * isArray([1,2,3]) // true
 * isArray({a: 1})  // false
 * isArray(1)       // false
 * isArray('test')  // false
 */
export const isArray = R.is(Array);

/**
 * @sig Object a => (* -> boolean) -> a -> Array [*]
 * @description Returns all values from deeply nested object
 */
export const values = R.curry((isValue, object) =>
  R.compose(
    R.flatten,
    R.map(
      R.ifElse(
        R.allPass([isObject, R.complement(isValue)]),
        object => values(isValue, object),
        R.identity
      )
    ),
    R.values
  )(object)
);

/**
 * @sig Functor a => (* -> boolean) -> (a -> b) -> a
 * @description Maps all values in deeply nested object
 */
export const map = R.curry((isValue, mapping, functor) =>
  R.map(
    R.ifElse(
      R.allPass([isObject, R.complement(isValue)]),
      object => map(isValue, mapping, object),
      mapping
    ),
    functor
  )
);

/**
 * @sig Functor a => (* -> boolean) -> (a -> boolean) -> a
 * @description Filters deeply nested object leaving only passing values
 */
export const filter = R.curry((isValue, predicate, filterable) =>
  R.compose(
    R.filter(
      R.ifElse(R.allPass([isObject, R.complement(isValue)]), R.T, predicate)
    ),
    R.map(
      R.ifElse(
        R.allPass([isObject, R.complement(isValue)]),
        f => filter(isValue, predicate, f),
        R.identity
      )
    )
  )(filterable)
);

/**
 * @member
 * @private
 * @sig a -> Array [*]
 * @description Transforms given object into array
 */
const toArray = object => {
  const result = Array(R.length(R.keys(object)));
  R.forEachObjIndexed((value, key) => {
    result[parseInt(key)] = value;
  }, object);
  return result;
};

/**
 * @sig (* -> boolean) -> a -> b -> c
 * @description Merges keys from left to right within deeply nested objects
 */
export const mergeRight = R.curry((isValue, left, right) =>
  R.compose(
    R.when(R.always(isArray(left) && isArray(right)), toArray),
    R.mergeWith(
      R.compose(
        R.ifElse(
          R.all(R.allPass([isObject, R.complement(isValue)])),
          ([l, r]) => mergeRight(isValue, l, r),
          R.nth(1)
        ),
        R.pair
      )
    )
  )(left, right)
);

/**
 * @member
 * @private
 * @sig string -> string -> string -> string
 * @description Adds prefix to a key separated by separator
 */
const addPrefix = (prefix, separator, key) =>
  R.isNil(prefix) ? key : prefix + separator + key;

/**
 * @private
 * @sig string -> string -> (* -> boolean) -> a -> b
 * @description Transforms deeply nested object to flat object with prefixed keys
 */
const treeFlatPrefixed = R.curry((prefix, separator, isValue, object) =>
  R.reduce(
    (result, [key, value]) =>
      R.is(Object, value) && !isValue(value)
        ? R.mergeRight(
            result,
            treeFlatPrefixed(
              addPrefix(prefix, separator, key),
              separator,
              isValue,
              value
            )
          )
        : R.assoc(addPrefix(prefix, separator, key), value, result),
    {},
    R.toPairs(object)
  )
);

/**
 * @member
 * @sig string -> (* -> boolean) -> a -> b
 * @description Transforms deeply nested object to flat object
 */
export const treeFlat = treeFlatPrefixed(null);
