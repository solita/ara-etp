import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const findCountryById = R.curry((id, countries) =>
  Maybe.fromNull(R.find(R.propEq('id', id), countries))
);

export const findCountry = R.curry((name, countries) =>
  R.compose(
    Maybe.fromNull,
    R.find(R.compose(
      R.includes(R.toLower(name)),
      R.map(R.toLower),
      R.props(['id', 'label-fi', 'label-sv'])))
  )(countries)
);
