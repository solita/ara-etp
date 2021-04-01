import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Query from '@Utility/query';
import * as dfns from 'date-fns';

import * as energiatodistusApi from '@Component/Energiatodistus/energiatodistus-api';

const url = {
  valvonta: '/api/private/valvonta/oikeellisuus',
  valvontaCount: '/api/private/valvonta/oikeellisuus/count'
};

export const deserializeValvonta = R.evolve({
  'create-time': dfns.parseJSON,
  'publish-time': dfns.parseJSON,
  'deadline-date': R.compose(R.map(dfns.parseISO), Maybe.fromNull),
  'diaarinumero': Maybe.fromNull,
  'energiatodistus': energiatodistusApi.deserialize
});

export const valvonnat = R.compose(
  R.map(R.map(deserializeValvonta)),
  Fetch.getJson(fetch),
  R.concat(url.valvonta),
  Query.toQueryString
);

export const valvontaCount = Fetch.getJson(fetch, url.valvontaCount);

export const toimenpidetyypit = Fetch.cached(fetch, '/valvonta/oikeellisuus/toimenpidetyypit')