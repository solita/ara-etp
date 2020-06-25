import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';

const api = 'api/private';

export const laskutuskielet = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(api + '/laskutuskielet/');

export const verkkolaskuoperaattorit = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(api + '/verkkolaskuoperaattorit/');

export const luokittelut = () => Future.parallelObject(2, {
  laskutuskielet: laskutuskielet,
  verkkolaskuoperaattorit: verkkolaskuoperaattorit
});
