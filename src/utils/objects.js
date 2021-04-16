import * as R from 'ramda';

export const mapKeys = R.curry((fn, object) =>
  R.compose(R.fromPairs, R.map(R.over(R.lensIndex(0), fn)), R.toPairs)(object)
);

export const renameKeys = R.curry((keysMap, obj) =>
  R.reduce((acc, key) => R.assoc(keysMap[key] || key, obj[key], acc), {}, R.keys(obj))
);

export const requireNotNil = R.curry((value, error) => {
  if (R.isNil(value)) {
    throw error;
  }
  return value;
});
