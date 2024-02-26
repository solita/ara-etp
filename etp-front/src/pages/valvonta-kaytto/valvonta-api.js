import * as R from 'ramda';
import * as Objects from '@Utility/objects';
import * as Either from '@Utility/either-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Parsers from '@Utility/parsers';
import * as EM from '@Utility/either-maybe';
import * as Query from '@Utility/query';
import * as dfns from 'date-fns';

import * as Toimenpiteet from './toimenpiteet';

import * as EtApi from '@Pages/energiatodistus/energiatodistus-api';

export const isAshaFailure = response =>
  response?.body?.type === 'asha-request-failed';

export const url = {
  valvonnat: 'api/private/valvonta/kaytto',
  valvonta: id => `${url.valvonnat}/${id}`,
  henkilot: kohdeId => `${url.valvonta(kohdeId)}/henkilot`,
  henkilo: (id, kohdeId) => `${url.henkilot(kohdeId)}/${id}`,
  yritykset: kohdeId => `${url.valvonta(kohdeId)}/yritykset`,
  yritys: (id, kohdeId) => `${url.yritykset(kohdeId)}/${id}`,
  toimenpiteet: id => `${url.valvonta(id)}/toimenpiteet`,
  previewHenkilo: (id, henkiloId) =>
    `${url.valvonta(id)}/toimenpiteet/henkilot/${henkiloId}/preview`,
  previewYritys: (id, yritysId) =>
    `${url.valvonta(id)}/toimenpiteet/yritykset/${yritysId}/preview`,
  toimenpide: (id, toimenpideId) => `${url.toimenpiteet(id)}/${toimenpideId}`,
  documentHenkilo: (henkiloId, id, toimenpideId, filename) =>
    `${url.toimenpide(
      id,
      toimenpideId
    )}/henkilot/${henkiloId}/document/${filename}`,
  documentYritys: (yritysId, id, toimenpideId, filename) =>
    `${url.toimenpide(
      id,
      toimenpideId
    )}/yritykset/${yritysId}/document/${filename}`,
  courtAttachmentHenkilo: (henkiloId, id, toimenpideId) =>
    `${url.toimenpide(
      id,
      toimenpideId
    )}/henkilot/${henkiloId}/attachment/hallinto-oikeus.pdf`,
  courtAttachmentYritys: (yritysId, id, toimenpideId) =>
    `${url.toimenpide(
      id,
      toimenpideId
    )}/yritykset/${yritysId}/attachment/hallinto-oikeus.pdf`,
  liitteet: id => `${url.valvonta(id)}/liitteet`,
  notes: id => `${url.valvonta(id)}/notes`
};

export const toimenpidetyypit = Fetch.cached(
  fetch,
  '/valvonta/kaytto/toimenpidetyypit'
);

export const ilmoituspaikat = Fetch.cached(
  fetch,
  '/valvonta/kaytto/ilmoituspaikat'
);
export const toimitustavat = Fetch.cached(
  fetch,
  '/valvonta/kaytto/toimitustavat'
);
export const roolit = Fetch.cached(fetch, '/valvonta/kaytto/roolit');

export const hallintoOikeudet = Fetch.cached(
  fetch,
  '/valvonta/kaytto/hallinto-oikeudet'
);

export const karajaoikeudet = Fetch.cached(
  fetch,
  '/valvonta/kaytto/karajaoikeudet'
);

export const johtaja = Fetch.getJson(
  fetch,
  'api/private/valvonta/kaytto/johtaja'
);

export const valvojat = Fetch.getJson(fetch, 'api/private/valvonta/valvojat');

export const templatesByType = R.compose(
  Future.cache,
  R.map(R.groupBy(R.prop('toimenpidetype-id'))),
  Fetch.getJson(fetch)
)(url.valvonnat + '/templates');

export const templates = R.compose(
  Future.cache,
  Fetch.getJson(fetch)
)(url.valvonnat + '/templates');
/* Osapuolten palvelut */

export const serializeOsapuoli = R.compose(
  R.evolve({
    'rooli-id': Maybe.orSome(null),
    maa: Maybe.orSome(null),
    'toimitustapa-id': Maybe.orSome(null),
    email: Maybe.getOrElse(null),
    jakeluosoite: Maybe.getOrElse(null),
    postinumero: Maybe.getOrElse(null),
    postitoimipaikka: Maybe.getOrElse(null),
    puhelin: Maybe.getOrElse(null),
    'rooli-description': Maybe.getOrElse(null),
    'toimitustapa-description': Maybe.getOrElse(null),
    'vastaanottajan-tarkenne': Maybe.getOrElse(null)
  }),
  R.dissoc('valvonta-id'),
  R.dissoc('id')
);

export const deserializeOsapuoli = R.evolve({
  'rooli-id': Maybe.fromNull,
  maa: Maybe.fromNull,
  'toimitustapa-id': Maybe.fromNull,
  email: Maybe.fromNull,
  jakeluosoite: Maybe.fromNull,
  postinumero: Maybe.fromNull,
  postitoimipaikka: Maybe.fromNull,
  puhelin: Maybe.fromNull,
  'rooli-description': Maybe.fromNull,
  'toimitustapa-description': Maybe.fromNull,
  'vastaanottajan-tarkenne': Maybe.fromNull
});

export const serializeHenkiloOsapuoli = R.compose(
  R.evolve({ henkilotunnus: Maybe.getOrElse(null) }),
  serializeOsapuoli
);

export const deserializeHenkiloOsapuoli = R.compose(
  R.evolve({ henkilotunnus: Maybe.fromNull }),
  deserializeOsapuoli
);

export const serializeYritysOsapuoli = R.compose(
  R.evolve({ ytunnus: Maybe.getOrElse(null) }),
  serializeOsapuoli
);

export const deserializeYritysOsapuoli = R.compose(
  R.evolve({ ytunnus: Maybe.fromNull }),
  deserializeOsapuoli
);

export const getHenkilot = R.compose(
  R.map(R.map(deserializeHenkiloOsapuoli)),
  Fetch.getJson(fetch),
  url.henkilot
);
export const getYritykset = R.compose(
  R.map(R.map(deserializeYritysOsapuoli)),
  Fetch.getJson(fetch),
  url.yritykset
);

export const getHenkilo = R.compose(
  R.map(deserializeHenkiloOsapuoli),
  Fetch.getJson(fetch),
  url.henkilo
);

export const postHenkilo = R.curry((valvontaId, henkilo) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'post', url.henkilot(valvontaId))
    ),
    serializeHenkiloOsapuoli
  )(henkilo)
);

export const putHenkilo = R.curry((valvontaId, id, henkilo) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'put', url.henkilo(id, valvontaId))
    ),
    serializeHenkiloOsapuoli
  )(henkilo)
);

export const deleteHenkilo = R.curry((valvontaId, id) =>
  Fetch.deleteFuture(url.henkilo(id, valvontaId))
);

export const getYritys = R.compose(
  R.map(deserializeYritysOsapuoli),
  Fetch.getJson(fetch),
  url.yritys
);

export const postYritys = R.curry((valvontaId, yritys) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'post', url.yritykset(valvontaId))
    ),
    serializeYritysOsapuoli
  )(yritys)
);

export const putYritys = R.curry((valvontaId, id, yritys) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'put', url.yritys(id, valvontaId))
    ),
    serializeYritysOsapuoli
  )(yritys)
);

export const deleteYritys = R.curry((valvontaId, id) =>
  Fetch.deleteFuture(url.yritys(id, valvontaId))
);

/* Toimenpiteen palvelut */

const deserializeToimenpide = R.evolve({
  'create-time': dfns.parseJSON,
  'publish-time': R.compose(R.map(dfns.parseJSON), Maybe.fromNull),
  'deadline-date': R.compose(
    Parsers.toEitherMaybe,
    R.map(Parsers.parseISODate),
    Maybe.fromNull
  ),
  diaarinumero: Maybe.fromNull,
  description: Maybe.fromNull,
  'template-id': Maybe.fromNull
});

/**
 * Sets field to none if document is false and the field exists
 */
const setFieldToNoneIfNoDocument = field => data => {
  return R.when(
    R.both(R.propEq('document', false), R.has(field)),
    R.set(R.lensProp(field), Maybe.None())
  )(data);
};

/**
 * Sets all given fields to none if document is false and the field exists
 */
const setFieldsToNoneIfNoDocument = fields => data =>
  R.reduce(
    (acc, field) => setFieldToNoneIfNoDocument(field)(acc),
    data,
    fields
  );

/**
 * Sets field to none if recipient-answered is false and the field exists
 */
const setFieldToNoneIfNoAnswer = field => data => {
  return R.when(
    R.both(R.propEq('recipient-answered', false), R.has(field)),
    R.set(R.lensProp(field), Maybe.None())
  )(data);
};

/**
 * Sets all given fields to none if recipient-answered is false and the field exists
 */
const setFieldsToNoneIfNoAnswer = fields => data =>
  R.reduce((acc, field) => setFieldToNoneIfNoAnswer(field)(acc), data, fields);

/**
 * Unwraps the maybe to the value if the field exists
 */
const unwrapMaybeIfExists = field => data => {
  return R.when(
    R.has(field),
    R.over(R.lensProp(field), Maybe.orSome(null))
  )(data);
};

/**
 * Unwraps the maybe to the value in all the given fields if the field exists
 */
const unwrapMaybesIfExists = fields => data =>
  R.reduce((acc, field) => unwrapMaybeIfExists(field)(acc), data, fields);

/**
 * Sets field to none if document is false and the field exists
 */
const setFieldToNullIfNoDocument = field => data => {
  return R.when(
    R.both(R.propEq('document', false), R.has(field)),
    R.set(R.lensProp(field), null)
  )(data);
};

/**
 * Removes all keys from object that have null or undefined values
 * @param {Object} object
 */
const removeNullValues = R.compose(
  R.fromPairs,
  R.reject(R.compose(R.isNil, R.prop(1))),
  R.toPairs
);

export const serializePenaltyDecisionActualDecisionOsapuoliSpecificData =
  osapuoliSpecificData => {
    return R.map(
      R.compose(
        removeNullValues,
        setFieldToNullIfNoDocument('recipient-answered'),
        unwrapMaybesIfExists([
          'answer-commentary-fi',
          'answer-commentary-sv',
          'statement-fi',
          'statement-sv',
          'hallinto-oikeus-id'
        ]),
        setFieldsToNoneIfNoDocument([
          'answer-commentary-fi',
          'answer-commentary-sv',
          'statement-fi',
          'statement-sv',
          'hallinto-oikeus-id'
        ])
      )
    )(osapuoliSpecificData);
  };

export const serializeOsapuoliSpecificData = osapuoliSpecificData => {
  return R.map(
    R.compose(
      removeNullValues,
      setFieldToNullIfNoDocument('recipient-answered'),
      unwrapMaybesIfExists([
        'answer-commentary-fi',
        'answer-commentary-sv',
        'statement-fi',
        'statement-sv',
        'hallinto-oikeus-id',
        'karajaoikeus-id',
        'haastemies-email'
      ]),
      setFieldsToNoneIfNoAnswer([
        'answer-commentary-fi',
        'answer-commentary-sv',
        'statement-fi',
        'statement-sv'
      ]),
      setFieldsToNoneIfNoDocument([
        'answer-commentary-fi',
        'answer-commentary-sv',
        'statement-fi',
        'statement-sv',
        'hallinto-oikeus-id',
        'karajaoikeus-id',
        'haastemies-email'
      ])
    )
  )(osapuoliSpecificData);
};

const serializeToimenpide = toimenpide =>
  R.compose(
    R.evolve({
      'template-id': Maybe.orSome(null),
      'severity-id': Maybe.orSome(null),
      description: Maybe.orSome(null),
      'deadline-date': EM.fold(null, date =>
        dfns.formatISO(date, { representation: 'date' })
      ),
      'type-specific-data': {
        fine: Maybe.orSome(null),
        'osapuoli-specific-data': Toimenpiteet.isPenaltyDecisionActualDecision(
          toimenpide
        )
          ? serializePenaltyDecisionActualDecisionOsapuoliSpecificData
          : serializeOsapuoliSpecificData,
        'department-head-title-fi': Maybe.orSome(null),
        'department-head-title-sv': Maybe.orSome(null),
        'department-head-name': Maybe.orSome(null)
      }
    }),
    R.pick([
      'type-id',
      'deadline-date',
      'description',
      'template-id',
      'bypass-asha',
      'type-specific-data'
    ])
  )(toimenpide);

export const toimenpiteet = R.compose(
  R.map(R.sortBy(Toimenpiteet.time)),
  R.map(
    R.map(
      R.evolve({
        henkilot: R.map(deserializeHenkiloOsapuoli),
        yritykset: R.map(deserializeYritysOsapuoli)
      })
    )
  ),
  R.map(R.map(deserializeToimenpide)),
  Fetch.getJson(fetch),
  url.toimenpiteet
);

export const toimenpide = R.compose(
  R.map(deserializeToimenpide),
  Fetch.getJson(fetch),
  url.toimenpide
);

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
    serializeToimenpide,
    R.omit(['type-specific-data', 'type-id'])
  )(toimenpide)
);

export const previewToimenpideForHenkiloOsapuoli = R.curry(
  (id, henkiloId, toimenpide) =>
    R.compose(
      Fetch.responseAsBlob,
      Future.encaseP(
        Fetch.fetchWithMethod(fetch, 'post', url.previewHenkilo(id, henkiloId))
      ),
      serializeToimenpide
    )(toimenpide)
);

export const previewToimenpideForYritysOsapuoli = R.curry(
  (id, yritysId, toimenpide) =>
    R.compose(
      Fetch.responseAsBlob,
      Future.encaseP(
        Fetch.fetchWithMethod(fetch, 'post', url.previewYritys(id, yritysId))
      ),
      serializeToimenpide
    )(toimenpide)
);

/* Valvonnan palvelut */

const deserializeValvonta = R.evolve({
  'ilmoituspaikka-id': Maybe.fromNull,
  'valvoja-id': Maybe.fromNull,
  'ilmoituspaikka-description': Maybe.fromNull,
  ilmoitustunnus: Maybe.fromNull,
  postinumero: Maybe.fromNull,
  rakennustunnus: Maybe.fromNull,
  havaintopaiva: R.compose(
    Parsers.toEitherMaybe,
    R.map(Parsers.parseISODate),
    Maybe.fromNull
  )
});

const serializeValvonta = R.compose(
  R.evolve({
    'valvoja-id': Maybe.orSome(null),
    'ilmoituspaikka-id': Maybe.orSome(null),
    'ilmoituspaikka-description': Maybe.orSome(null),
    ilmoitustunnus: Maybe.orSome(null),
    postinumero: Maybe.orSome(null),
    rakennustunnus: Maybe.orSome(null),
    havaintopaiva: EM.fold(null, date =>
      dfns.formatISO(date, { representation: 'date' })
    )
  }),
  R.omit(['id'])
);

const deserializeValvontaStatus = R.compose(
  R.evolve({
    lastToimenpide: R.compose(R.map(deserializeToimenpide), Maybe.fromNull),
    henkilot: R.map(deserializeHenkiloOsapuoli),
    yritykset: R.map(deserializeYritysOsapuoli),
    energiatodistus: Maybe.fromNull
  }),
  deserializeValvonta,
  Objects.renameKeys({ 'last-toimenpide': 'lastToimenpide' })
);

export const valvonnat = R.compose(
  R.map(R.map(deserializeValvontaStatus)),
  Fetch.getJson(fetch),
  R.concat(url.valvonnat),
  Query.toQueryString
);

export const valvontaCount = R.compose(
  Fetch.getJson(fetch),
  R.concat(`${url.valvonnat}/count`),
  Query.toQueryString
);

export const valvonta = R.compose(
  R.map(deserializeValvonta),
  Fetch.getJson(fetch),
  url.valvonta
);

export const postValvonta = R.curry((fetch, valvonta) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.valvonnat)),
    serializeValvonta
  )(valvonta)
);

export const putValvonta = R.curry((id, valvonta) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.valvonta(id))),
    serializeValvonta
  )(valvonta)
);

export const deleteValvonta = R.compose(Fetch.deleteFuture, url.valvonta);

/* Muistiinpanojen palvelut */

const deserializeNote = R.evolve({
  'create-time': dfns.parseJSON
});

export const postNote = R.curry((id, note) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.notes(id))),
    R.prop('description')
  )(note)
);

export const notes = R.compose(
  R.map(R.map(deserializeNote)),
  Fetch.getJson(fetch),
  url.notes
);

/* Liitteiden palvelut */

export const liitteet = R.compose(
  R.map(R.map(EtApi.deserializeLiite)),
  Fetch.getJson(fetch),
  url.liitteet
);

export const postLiitteetFiles = R.curry((valvontaId, files) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(files =>
      fetch(url.liitteet(valvontaId) + '/files', {
        method: 'POST',
        body: EtApi.toFormData('files', files)
      })
    )
  )(files)
);

export const postLiitteetLink = R.curry((valvontaId, link) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'post', url.liitteet(valvontaId) + '/link')
    )
  )(link)
);

export const deleteLiite = R.curry((valvontaId, liiteId) =>
  Fetch.deleteFuture(url.liitteet(valvontaId) + '/' + liiteId)
);

const deserializeExistingValvonnat = R.map(
  R.evolve({
    id: parseInt,
    'end-time': R.compose(
      R.chain(Either.toMaybe),
      R.map(Parsers.parseISODate),
      Maybe.fromNull
    )
  })
);

export const getExistingValvonnatByRakennusTunnus = rakennustunnus =>
  R.map(
    deserializeExistingValvonnat,
    Fetch.cached(fetch, `/valvonta/kaytto/rakennustunnus/${rakennustunnus}/`)
  );
