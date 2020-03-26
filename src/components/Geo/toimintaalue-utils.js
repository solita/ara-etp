import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const formatSelected = R.reduce((acc, i) => R.assoc(i, true, acc), {});

export const toimintaAlueetToSelect = R.converge(R.zipObj, [
  R.pluck('id'),
  R.compose(R.repeat(false), R.inc, R.reduce(R.max, -Infinity), R.pluck('id'))
]);

export const findToimintaAlueById = R.curry((id, toimintaAlueet) =>
  R.compose(Maybe.fromNull, R.find(R.propEq('id', id)))(toimintaAlueet)
);

export const findToimintaAlue = R.curry((toimintaAlueet, label) =>
  R.compose(
    Maybe.fromNull,
    R.find(R.compose(R.includes(R.toLower(label)), R.map(R.toLower), R.values))
  )(toimintaAlueet)
);
