import * as R from 'ramda';

export const inRangeInclusive = R.curry((low, high, value) =>
  R.allPass([R.gte(high), R.lte(low)])(value)
);
