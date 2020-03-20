import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const findYritysByYtunnus = R.curry((yritykset, ytunnus) =>
  Maybe.fromUndefined(R.find(R.propEq('ytunnus', ytunnus), yritykset)));

export const findYritys = R.curry((yritykset, query) =>
  Maybe.fromUndefined(
    R.find(R.compose(
        R.includes(R.toLower(query)),
        R.map(R.toLower),
        R.values,
        R.pick(['nimi', 'ytunnus', 'jakeluosoite'])),
      yritykset)));
