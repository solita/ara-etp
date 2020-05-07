import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';

const api = 'api/private';

export const countries = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(api + '/countries/');

export const toimintaalueet = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(api + '/toimintaalueet/');