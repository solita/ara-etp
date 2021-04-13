import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Parsers from '@Utility/parsers';
import * as EM from '@Utility/either-maybe';
import * as Query from '@Utility/query';
import * as dfns from 'date-fns';

import * as energiatodistusApi from '@Component/Energiatodistus/energiatodistus-api';

const url = {
  valvonnat: 'api/private/valvonta/oikeellisuus',
  valvonta: id => `${url.valvonnat}/${id}`,
  toimenpiteet: id => `${url.valvonta(id)}/toimenpiteet`
};

export const deserializeValvonta = R.evolve({
  'create-time': dfns.parseJSON,
  'publish-time': dfns.parseJSON,
  'deadline-date': Parsers.optionalParser(Parsers.parseISODate),
  'diaarinumero': Maybe.fromNull,
  'energiatodistus': energiatodistusApi.deserialize
});

export const deserializeToimenpide = R.evolve({
  'create-time': dfns.parseJSON,
  'publish-time': dfns.parseJSON,
  'deadline-date': R.compose(R.map(dfns.parseISO), Maybe.fromNull),
  'diaarinumero': Maybe.fromNull,
});

export const deserializeValvontaState = R.evolve({
  'valvoja-id': Maybe.fromNull
});

export const serializeToimenpide = R.evolve({
  'document': Maybe.orSome(null),
  'deadline-date': EM.fold(null, date => dfns.formatISO(date, { representation: 'date' }))
});

export const valvonnat = R.compose(
  R.map(R.map(deserializeValvonta)),
  Fetch.getJson(fetch),
  R.concat(url.valvonnat),
  Query.toQueryString
);

export const valvontaCount = Fetch.getJson(fetch, url.valvonnat + '/count');

export const toimenpidetyypit = Fetch.cached(fetch, '/valvonta/oikeellisuus/toimenpidetyypit');

export const toimenpiteet = R.compose(
  R.map(R.map(deserializeToimenpide)),
  Fetch.getJson(fetch),
  url.toimenpiteet
);

export const valvonta = R.compose(
  R.map(deserializeValvontaState),
  Fetch.getJson(fetch),
  url.valvonta
);

export const valvojat = Fetch.getJson(fetch, url.valvonnat + '/valvojat');

export const postToimenpide = (id, toimenpide) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.toimenpiteet(id))),
    serializeToimenpide
  )(toimenpide);

export const putValvonta= R.curry((id, body) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.valvonta(id)))
  )(body)
);