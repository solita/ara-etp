import * as R from 'ramda';

/**
 * This library contains implementations of some rambda functionality
 * for nested object structures.
 */

export const isObject = R.is(Object);

export const values = R.curry((isValue, object) => R.compose(
  R.flatten,
  R.map(R.ifElse(R.allPass([isObject, R.complement(isValue)]),
        object => values(isValue, object),
        R.identity)),
  R.values)(object));

export const map = R.curry((isValue, fn, functor) =>
  R.map(R.ifElse(R.allPass([isObject, R.complement(isValue)]),
    object => map(isValue, fn, object), fn),
    functor));