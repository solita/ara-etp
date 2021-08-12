/**
 * @module RamdaUtils
 * @description Generic Ramda based utilities
 */

import * as R from 'ramda';

/**
 * @sig number -> number -> boolean
 */
export const inRangeInclusive = R.curry((low, high, value) =>
  R.allPass([R.gte(high), R.lte(low)])(value)
);

/**
 * @sig number -> a -> Array a -> Array a
 */
export const fillAndTake = R.curry((n, item, arr) =>
  R.compose(R.take(n), R.concat(arr), R.times)(item, n)
);
