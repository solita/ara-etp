import * as R from 'ramda';

export const inRangeInclusive = R.curry((low, high, value) =>
  R.allPass([R.gte(high), R.lte(low)])(value)
);

export const fillAndTake = R.curry((n, item, arr) =>
  R.compose(R.take(n), R.concat(arr), R.times)(item, n)
);
