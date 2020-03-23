import * as R from 'ramda';

export const formatSelected = R.reduce((acc, i) => R.assoc(i, true, acc), {});

export const toimintaAlueetToSelect = R.converge(R.zipObj, [
  R.pluck('id'),
  R.compose(R.repeat(false), R.inc, R.reduce(R.max, -Infinity), R.pluck('id'))
]);
