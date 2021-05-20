import * as R from 'ramda';
import * as Objects from '@Utility/objects';
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
  toimenpiteet: id => `${url.valvonta(id)}/toimenpiteet`,
  toimenpide: (id, toimenpideId) => `${url.toimenpiteet(id)}/${toimenpideId}`
};

export const deserializeToimenpide = R.evolve({
  'create-time': dfns.parseJSON,
  'publish-time': R.compose(R.map(dfns.parseJSON), Maybe.fromNull),
  'deadline-date': R.compose(
    Parsers.toEitherMaybe,
    R.map(Parsers.parseISODate),
    Maybe.fromNull
  ),
  diaarinumero: Maybe.fromNull,
  description: Maybe.fromNull,
  'template-id': Maybe.fromNull,
  'severity-id': Maybe.fromNull
});

export const deserializeValvontaStatus = R.compose(
  R.evolve({
    lastToimenpide: R.compose(R.map(deserializeToimenpide), Maybe.fromNull),
    energiatodistus: energiatodistusApi.deserialize
  }),
  Objects.renameKeys({ 'last-toimenpide': 'lastToimenpide' })
);

export const deserializeValvonta = R.evolve({
  'valvoja-id': Maybe.fromNull
});

export const serializeToimenpide = R.compose(
  R.evolve({
    'template-id': Maybe.orSome(null),
    'severity-id': Maybe.orSome(null),
    description: Maybe.orSome(null),
    'deadline-date': EM.fold(null, date =>
      dfns.formatISO(date, { representation: 'date' })
    )
  }),
  R.pick([
    'type-id',
    'deadline-date',
    'description',
    'template-id',
    'virheet',
    'severity-id'
  ])
);

export const valvonnat = R.compose(
  R.map(R.map(deserializeValvontaStatus)),
  Fetch.getJson(fetch),
  R.concat(url.valvonnat),
  Query.toQueryString
);

export const valvontaCount = Fetch.getJson(fetch, url.valvonnat + '/count');

export const toimenpidetyypit = Fetch.cached(
  fetch,
  '/valvonta/oikeellisuus/toimenpidetyypit'
);

export const virhetyypit = Fetch.cached(
  fetch,
  '/valvonta/oikeellisuus/virhetyypit'
);

export const severities = Fetch.cached(
  fetch,
  '/valvonta/oikeellisuus/severities'
);

export const templatesByType = R.compose(
  Future.cache,
  R.map(R.groupBy(R.prop('toimenpidetype-id'))),
  Fetch.getJson(fetch)
)(url.valvonnat + '/templates');

export const toimenpiteet = R.compose(
  R.map(R.map(deserializeToimenpide)),
  Fetch.getJson(fetch),
  url.toimenpiteet
);

export const toimenpide = R.compose(
  R.map(deserializeToimenpide),
  Fetch.getJson(fetch),
  url.toimenpide
);

export const valvonta = R.compose(
  R.map(deserializeValvonta),
  Fetch.getJson(fetch),
  url.valvonta
);

export const valvojat = Fetch.getJson(fetch, 'api/private/valvonta/valvojat');

export const postToimenpide = R.curry((id, toimenpide) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.toimenpiteet(id))),
    serializeToimenpide
  )(toimenpide)
);

export const putToimenpide = R.curry((id, toimenpideId, toimenpide) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'put', url.toimenpide(id, toimenpideId))
    ),
    R.dissoc('type-id'),
    serializeToimenpide
  )(toimenpide)
);

export const putValvonta = R.curry((id, body) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.valvonta(id)))
  )(body)
);

export const publishToimenpide = R.curry((id, toimenpideId) =>
  R.chain(
    Fetch.rejectWithInvalidResponse,
    Future.attemptP(_ =>
      Fetch.postEmpty(fetch, url.toimenpide(id, toimenpideId) + '/publish')
    )
  )
);
