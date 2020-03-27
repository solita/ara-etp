import * as R from 'ramda';

export const isObject = R.is(Object);

export const recursiveValues = R.curry((isValue, object) => R.compose(
  R.flatten,
  R.map(R.ifElse(R.allPass([isObject, R.complement(isValue)]),
        object => recursiveValues(isValue, object),
        R.identity)),
  R.values)(object));