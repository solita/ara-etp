import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as deep from '@Utility/deep-objects';
import * as empty from './empty';
import * as schema from './schema';
import * as laatijaApi from '@Component/Laatija/laatija-api';
import * as yritysApi from '@Component/Yritys/yritys-api';
import * as dfns from "date-fns";

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
  laskutusaika: Maybe.map(dfns.parseJSON),
  perustiedot: {
    'julkinen-rakennus': Maybe.get,
    uudisrakennus: Maybe.get
  }
};

const transformationFromSchema = name => R.compose(
  deep.filter(R.is(Function), R.complement(R.isNil)),
  deep.map(
    R.propSatisfies(R.is(Array), 'validators'),
    R.prop(name)));

const versions = {
  "2018": {
    deserializer: transformationFromSchema('deserialize')(schema.v2018),
    serializer: transformationFromSchema('serialize')(schema.v2018)
  },
  "2013": {
    deserializer: transformationFromSchema('deserialize')(schema.v2013),
    serializer: transformationFromSchema('serialize')(schema.v2013)
  }
}

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
    [R.propEq('versio', 2018), mergeEmpty(empty.energiatodistus2018)],
    [R.propEq('versio', 2013), mergeEmpty(empty.energiatodistus2013)]
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

export const url = {
  all: '/api/private/energiatodistukset',
  version: version => `${url.all}/${version}`,
  id: (version, id) => `${url.version(version)}/${id}`,
  liitteet: (version, id) => `${url.id(version, id)}/liitteet`,
  signature: (version, id) => `${url.id(version, id)}/signature`,
  start: (version, id) => `${url.signature(version, id)}/start`,
  digest: (version, id) => `${url.signature(version, id)}/digest`,
  pdf: (version, id) => `${url.signature(version, id)}/pdf`,
  finish: (version, id) => `${url.signature(version, id)}/finish`
};

export const toQueryString = R.compose(
  Maybe.orSome(''),
  R.map(R.compose(R.concat('?'), R.join('&'))),
  Maybe.toMaybeList,
  R.map(([key, optionalValue]) =>
    R.map(value => `${key}=${value}`, optionalValue)
  ),
  R.toPairs
);

export const getEnergiatodistukset = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  R.concat(url.all)
);

export const getEnergiatodistusById = R.curry((fetch, version, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.id
  )(version, id)
);

export const getLiitteetById = R.curry((fetch, version, id) =>
  R.compose(
    R.map(R.map(deserializeLiite)),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.liitteet
  )(version, id)
);

const toFormData = (name, files) => {
  const data = new FormData();
  R.forEach(file => {
    data.append(name, file);
  }, files);
  return data;
};

export const postLiitteetFiles = R.curry((fetch, version, id, files) =>
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

export const postLiitteetLink = R.curry((fetch, version, id, link) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'post', url.liitteet(version, id) + '/link')
    )
  )(link)
);

export const deleteLiite = R.curry((fetch, version, id, liiteId) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(liiteId =>
      Fetch.deleteRequest(fetch, url.liitteet(version, id) + '/' + liiteId)
    )
  )(liiteId)
);

export const putEnergiatodistusById = R.curry(
  (fetch, version, id, energiatodistus) =>
    R.compose(
      R.chain(Fetch.rejectWithInvalidResponse),
      Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.id(version, id))),
      serialize
    )(energiatodistus)
);

export const startSign = R.curry((fetch, version, id) =>
  R.compose(
    R.chain(
      R.ifElse(
        R.compose(R.not, R.equals(409), R.prop('status')),
        R.compose(Fetch.responseAsText, Future.resolve),
        R.compose(R.chain(Fetch.toText), Future.resolve)
      )
    ),
    Future.encaseP(Fetch.postEmpty(fetch)),
    url.start
  )(version, id)
);

export const digest = R.curry((fetch, version, id) =>
  R.compose(
    Fetch.responseAsText,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.digest
  )(version, id)
);

export const signPdf = R.curry((fetch, version, id, signature) =>
  R.compose(
    Fetch.responseAsText,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.pdf(version, id)))
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

export const laatimisvaiheet = Fetch.cached(fetch, '/laatimisvaiheet');

const kayttotarkoitusluokat = version =>
  Fetch.cached(fetch, '/kayttotarkoitusluokat/' + version);
const alakayttotarkoitusluokat = version =>
  Fetch.cached(fetch, '/alakayttotarkoitusluokat/' + version);

export const luokittelut = R.memoizeWith(R.identity, version =>
  Future.parallelObject(7, {
    lammonjako: Fetch.cached(fetch, '/lammonjako'),
    lammitysmuoto: Fetch.cached(fetch, '/lammitysmuoto'),
    ilmanvaihtotyypit: Fetch.cached(fetch, '/ilmanvaihtotyyppi'),
    kielisyys: Fetch.cached(fetch, '/kielisyys'),
    laatimisvaiheet,
    kayttotarkoitusluokat: kayttotarkoitusluokat(version),
    alakayttotarkoitusluokat: alakayttotarkoitusluokat(version)
  })
);

export const replaceable = R.curry((fetch, id) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(`api/private/energiatodistukset/replaceable?id=${id}`)
);

export const getLaatijaYritykset = R.curry((fetch, laatijaId) =>
  Future.chain(
    R.compose(Future.parallel(10), R.map(yritysApi.getYritysById(fetch))),
    laatijaApi.getYritykset(fetch, laatijaId)
  )
);

export const validation = R.memoizeWith(R.identity, version =>
  Future.parallelObject(5, {
    numeric: Fetch.cached(fetch, '/validation/numeric/' + version),
    required: Fetch.cached(fetch, '/validation/required/' + version),
    kuormat: Fetch.cached(fetch, '/validation/sisaiset-kuormat/' + version)
  })
);
