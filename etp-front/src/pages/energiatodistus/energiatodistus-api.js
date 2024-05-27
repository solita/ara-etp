import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as deep from '@Utility/deep-objects';
import * as empty from './empty';
import * as schema from './schema';
import * as geoApi from '@Utility/api/geo-api';
import * as dfns from 'date-fns';
import * as Query from '@Utility/query';

/*
This deserializer is for all energiatodistus versions.

By default, every value is in Maybe.

If key does not exists in the specific version
then the deserialize function is not used
 */
const deserializer = {
  id: Maybe.get,
  versio: Maybe.get,
  'tila-id': Maybe.get,
  'draft-visible-to-paakayttaja': Maybe.get,
  'bypass-validation-limits': Maybe.get,
  laskutusaika: Maybe.map(dfns.parseJSON),
  allekirjoitusaika: Maybe.map(dfns.parseJSON),
  'voimassaolo-paattymisaika': Maybe.map(dfns.parseJSON),
  perustiedot: {
    'julkinen-rakennus': Maybe.get,
    uudisrakennus: Maybe.get
  }
};

const transformationFromSchema = name =>
  R.compose(
    deep.filter(R.is(Function), R.complement(R.isNil)),
    deep.map(R.propSatisfies(R.is(Array), 'validators'), R.prop(name))
  );

const versions = {
  2018: {
    deserializer: transformationFromSchema('deserialize')(schema.v2018),
    serializer: transformationFromSchema('serialize')(schema.v2018)
  },
  2013: {
    deserializer: transformationFromSchema('deserialize')(schema.v2013),
    serializer: transformationFromSchema('serialize')(schema.v2013)
  }
};

const evolveForVersion = name => energiatodistus =>
  R.evolve(versions[energiatodistus.versio][name], energiatodistus);

const assertVersion = et => {
  if (!R.includes(et.versio, R.map(parseInt, R.keys(versions)))) {
    throw 'Unsupported energiatodistus version: ' + et.versio;
  }
};

const mergeEmpty = deep.mergeRight(R.anyPass([Either.isEither, Maybe.isMaybe]));

export const deserialize = R.compose(
  R.cond([
    [R.propEq(2018, 'versio'), mergeEmpty(empty.energiatodistus2018())],
    [R.propEq(2013, 'versio'), mergeEmpty(empty.energiatodistus2013())]
  ]),
  evolveForVersion('deserializer'),
  R.tap(assertVersion),
  R.evolve(deserializer),
  deep.map(R.F, Maybe.fromNull)
);

export const serialize = R.compose(
  deep.map(
    Either.isEither,
    R.ifElse(
      R.allPass([R.complement(R.isNil), Either.isEither]),
      Either.orSome(null),
      R.identity
    )
  ),
  deep.map(
    Maybe.isMaybe,
    R.ifElse(
      R.allPass([R.complement(R.isNil), Maybe.isMaybe]),
      Maybe.orSome(null),
      R.identity
    )
  ),
  R.dissocPath(['tulokset', 'e-luku']),
  R.dissocPath(['tulokset', 'e-luokka']),
  R.omit([
    'id',
    'versio',
    'tila-id',
    'laatija-id',
    'laatija-fullname',
    'allekirjoitusaika',
    'laskutusaika',
    'korvaava-energiatodistus-id',
    'voimassaolo-paattymisaika'
  ]),
  evolveForVersion('serializer'),
  R.tap(assertVersion)
);

export const deserializeLiite = R.evolve({
  url: Maybe.fromNull,
  createtime: Date.parse
});

export const deserializeHistoryEvent = R.evolve({
  'init-v': Maybe.fromNull,
  'new-v': Maybe.fromNull,
  modifytime: dfns.parseISO
});

export const deserializeHistory = R.evolve({
  'state-history': R.map(deserializeHistoryEvent),
  'form-history': R.map(deserializeHistoryEvent)
});

export const url = {
  all: '/api/private/energiatodistukset',
  allCount: '/api/private/energiatodistukset/count',
  version: version => `${url.all}/${version}`,
  id: (version, id) => `${url.version(version)}/${id}`,
  pdf: (version, id, language) =>
    `${url.id(
      version,
      id
    )}/pdf/${language}/energiatodistus-${id}-${language}.pdf`,
  liitteet: (version, id) => `${url.id(version, id)}/liitteet`,
  history: id => `${url.all}/all/${id}/history`,
  signature: (version, id) => `${url.id(version, id)}/signature`,
  start: (version, id) => `${url.signature(version, id)}/start`,
  digest: (version, id, language) =>
    `${url.signature(version, id)}/digest/${language}`,
  signPdf: (version, id, language) =>
    `${url.signature(version, id)}/pdf/${language}`,
  signPdfsUsingSystemSignature: (version, id) =>
    `${url.signature(version, id)}/system-sign`,
  finish: (version, id) => `${url.signature(version, id)}/finish`,
  cancel: (version, id) => `${url.signature(version, id)}/cancel`,
  discarded: (version, id) => `${url.id(version, id)}/discarded`
};

export const getEnergiatodistukset = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  R.concat(url.all)
);

export const getEnergiatodistuksetCount = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  R.concat(url.allCount)
);

export const getEnergiatodistuksetByField = R.curry((field, value) =>
  getEnergiatodistukset(`?where=[[["=","${field}",${R.toString(value)}]]]`)
);

export const getEnergiatodistusById = R.curry((version, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.id
  )(version, id)
);

export const findEnergiatodistusById = R.compose(
  Future.chainRej(
    R.ifElse(
      R.propEq(404, 'status'),
      R.always(Future.resolve(null)),
      Future.reject
    )
  ),
  getEnergiatodistusById('all')
);

export const liitteet = R.compose(
  R.map(R.map(deserializeLiite)),
  Fetch.getJson(fetch),
  url.liitteet
);

export const toFormData = (name, files) => {
  const data = new FormData();
  R.forEach(file => {
    data.append(name, file);
  }, files);
  return data;
};

export const postLiitteetFiles = R.curry((version, id, files) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(uri =>
      fetch(uri + '/files', {
        method: 'POST',
        body: toFormData('files', files)
      })
    ),
    url.liitteet
  )(version, id)
);

export const postLiitteetLink = R.curry((version, id, link) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'post', url.liitteet(version, id) + '/link')
    )
  )(link)
);

export const deleteLiite = R.curry((version, id, liiteId) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(liiteId =>
      Fetch.deleteRequest(fetch, url.liitteet(version, id) + '/' + liiteId)
    )
  )(liiteId)
);

export const getEnergiatodistusHistoryById = R.curry(id =>
  R.compose(
    R.map(deserializeHistory),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.history
  )(id)
);

export const putEnergiatodistusById = R.curry(
  (fetch, version, id, energiatodistus) =>
    R.compose(
      R.chain(Fetch.rejectWithInvalidResponse),
      Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.id(version, id))),
      serialize
    )(energiatodistus)
);

const idempotentStateChange = R.curry((url, fetch, version, id) =>
  R.compose(
    R.chain(
      R.ifElse(
        R.compose(R.not, R.equals(409), R.prop('status')),
        R.compose(Fetch.responseAsText, Future.resolve),
        R.compose(R.chain(Fetch.toText), Future.resolve)
      )
    ),
    Future.encaseP(Fetch.postEmpty(fetch)),
    url
  )(version, id)
);

export const startSign = idempotentStateChange(url.start);
export const cancelSign = idempotentStateChange(url.cancel);

export const digest = R.curry((fetch, version, id, language) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.digest
  )(version, id, language)
);

export const signPdf = R.curry((fetch, version, id, language, signature) =>
  R.compose(
    Fetch.responseAsText,
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'put', url.signPdf(version, id, language))
    )
  )(signature)
);

export const finishSign = R.curry((fetch, version, id) =>
  R.compose(
    Fetch.responseAsText,
    Future.encaseP(Fetch.postEmpty(fetch)),
    url.finish
  )(version, id)
);

export const postEnergiatodistus = R.curry((fetch, version, energiatodistus) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.version(version))),
    serialize
  )(energiatodistus)
);

export const deleteEnergiatodistus = R.curry((fetch, version, id) =>
  R.chain(
    Fetch.rejectWithInvalidResponse,
    Future.attemptP(Fetch.fetchWithMethod(fetch, 'delete', url.id(version, id)))
  )
);

export const discardEnergiatodistus = R.curry((fetch, version, id) =>
  R.chain(
    Fetch.rejectWithInvalidResponse,
    Future.attemptP(_ =>
      Fetch.fetchWithMethod(fetch, 'post', url.discarded(version, id), true)
    )
  )
);

export const undoDiscardEnergiatodistus = R.curry((fetch, version, id) =>
  R.chain(
    Fetch.rejectWithInvalidResponse,
    Future.attemptP(_ =>
      Fetch.fetchWithMethod(fetch, 'post', url.discarded(version, id), false)
    )
  )
);

const luokittelut = {
  lammonjako: Fetch.cached(fetch, '/lammonjako'),
  lammitysmuoto: Fetch.cached(fetch, '/lammitysmuoto'),
  ilmanvaihtotyypit: Fetch.cached(fetch, '/ilmanvaihtotyyppi'),
  postinumerot: geoApi.postinumerot,
  kielisyys: Fetch.cached(fetch, '/kielisyys'),
  laatimisvaiheet: Fetch.cached(fetch, '/laatimisvaiheet'),
  patevyydet: Fetch.cached(fetch, '/patevyydet')
};

const kayttotarkoitusluokittelut = R.memoizeWith(R.identity, version => ({
  kayttotarkoitusluokat: Fetch.cached(
    fetch,
    '/kayttotarkoitusluokat/' + version
  ),
  alakayttotarkoitusluokat: Fetch.cached(
    fetch,
    '/alakayttotarkoitusluokat/' + version
  )
}));

export const luokittelutForVersion = version =>
  Future.parallelObject(8, {
    ...luokittelut,
    ...kayttotarkoitusluokittelut(version)
  });

export const luokittelutAllVersions = Future.parallelObject(10, {
  ...luokittelut,
  2018: Future.parallelObject(2, kayttotarkoitusluokittelut(2018)),
  2013: Future.parallelObject(2, kayttotarkoitusluokittelut(2013))
});

export const replaceable = R.curry((fetch, id) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(`api/private/energiatodistukset/replaceable?id=${id}`)
);

export const validation = R.memoizeWith(R.identity, version =>
  Future.parallelObject(5, {
    numeric: Fetch.cached(fetch, '/validation/numeric/' + version),
    requiredAll: Fetch.cached(
      fetch,
      '/validation/required/' + version + '/all'
    ),
    requiredBypass: Fetch.cached(
      fetch,
      '/validation/required/' + version + '/bypass'
    ),
    kuormat: Fetch.cached(fetch, '/validation/sisaiset-kuormat/' + version)
  })
);

export const getEluokka = R.curry(
  (fetch, versio, alakayttotarkoitusId, nettoala, eLuku) =>
    R.compose(
      Fetch.responseAsJson,
      Future.encaseP(Fetch.getFetch(fetch))
    )(
      `api/private/e-luokka/${versio}/${alakayttotarkoitusId}/${nettoala}/${eLuku}`
    )
);

export const korvattavat = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  R.concat(url.all + '/korvattavat'),
  Query.toQueryString
);

export const signPdfsUsingSystemSignature = (fetch, version, id) =>
  R.compose(
    Fetch.responseAsText,
    Future.encaseP(
      Fetch.fetchWithMethod(
        fetch,
        'post',
        url.signPdfsUsingSystemSignature(version, id)
      )
    )
  )();
