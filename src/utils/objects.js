/**
 * @module Objects
 * @description Generic utilities for
 */
import * as R from 'ramda';

/**
 * @sig ({string|number} -> {string|number}) -> Object -> Object
 */
export const mapKeys = R.curry((fn, object) =>
  R.compose(R.fromPairs, R.map(R.over(R.lensIndex(0), fn)), R.toPairs)(object)
);

/**
 * @sig Object -> Object -> Object
 */
export const renameKeys = R.curry((keysMap, obj) =>
  R.reduce(
    (acc, key) => R.assoc(keysMap[key] || key, obj[key], acc),
    {},
    R.keys(obj)
  )
);

export const requireNotNil = R.curry((value, error) => {
  if (R.isNil(value)) {
    throw error;
  }
  return value;
});
