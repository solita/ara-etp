import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const formatCountry = R.curry((countries, id) =>
  R.find(R.propEq('id', id), countries)
);

export const findCountry = R.curry((name, countries) =>
  R.compose(
    Maybe.fromNull,
    R.find(R.compose(R.includes(name), R.values))
  )(countries)
);
