import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const findToimintaAlueById = R.curry((id, toimintaAlueet) =>
  R.compose(Maybe.fromNull, R.find(R.propEq('id', id)))(toimintaAlueet)
);

export const toimintaalueetWithoutMain = R.curry(
  (mainToimintaalue, toimintaalueet) =>
    R.compose(
      Maybe.orSome(toimintaalueet),
      R.map(R.compose(R.applyTo(toimintaalueet), R.reject, R.equals))
    )(mainToimintaalue)
);

export const isMainToimintaAlue = R.curry((mainToimintaalue, toimintaalue) =>
  R.compose(Maybe.isSome, R.filter(R.equals(toimintaalue)))(mainToimintaalue)
);
