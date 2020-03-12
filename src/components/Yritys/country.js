import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const findCountry = R.curry((countries, countryName) =>
  Maybe.fromUndefined(R.find(R.compose(
      R.includes(countryName),
      R.values), countries)));

export const formatCountry = R.curry((countries, id) =>
  R.find(R.propEq('id', id), countries));
