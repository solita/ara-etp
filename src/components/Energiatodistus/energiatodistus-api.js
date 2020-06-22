import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as deep from '@Utility/deep-objects';
import * as empty from './empty';

const deserialize2018 = {
  id: Maybe.get,
  'tila-id': Maybe.get,
  perustiedot: {
    'onko-julkinen-rakennus': Maybe.get,
    valmistumisvuosi: Either.Right
  }
};

const deserialize2013 = R.mergeRight(deserialize2018, {
  perustiedot: { uudisrakennus: Maybe.get }
});

const assertVersion = et => {
  if (!R.includes(et.versio, [2018, 2013])) {
    throw 'Unsupported energiatodistus version: ' + et.version;
  }
};

const mergeEmpty = deep.mergeRight(R.anyPass([Either.isEither, Maybe.isMaybe]));

export const deserialize = R.compose(
  R.cond([
    [R.propEq('versio', 2018), mergeEmpty(empty.energiatodistus2018)],
    [R.propEq('versio', 2013), mergeEmpty(empty.energiatodistus2013)]
  ]),
  R.cond([
    [R.propEq('versio', 2018), R.evolve(deserialize2018)],
    [R.propEq('versio', 2013), R.evolve(deserialize2013)]
  ]),
  R.tap(assertVersion),
  R.evolve({ versio: Maybe.get }),
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
    'tila-id',
    'laatija-id',
    'laatija-fullname',
    'versio',
    'allekirjoitusaika',
    'allekirjoituksessaaika'
  ])
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
  Maybe.map(R.concat('?')),
  Maybe.map(R.join('&')),
  Maybe.toMaybeList,
  R.map(([key, optionalValue]) =>
    Maybe.map(value => `${key}=${value}`, optionalValue)
  ),
  R.toPairs
);

export const getEnergiatodistukset = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  R.concat(url.all),
  toQueryString
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

export const kielisyys = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)('api/private/kielisyys');

export const laatimisvaiheet = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)('api/private/laatimisvaiheet');

export const kayttotarkoitusluokat = version =>
  R.compose(
    Future.cache,
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/kayttotarkoitusluokat/' + version);

export const alakayttotarkoitusluokat = version =>
  R.compose(
    Future.cache,
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/alakayttotarkoitusluokat/' + version);

export const luokittelut = version =>
  Future.parallelObject(5, {
    kielisyys: kielisyys,
    laatimisvaiheet: laatimisvaiheet,
    kayttotarkoitusluokat: kayttotarkoitusluokat(version),
    alakayttotarkoitusluokat: alakayttotarkoitusluokat(version)
  });

export const signed = R.curry((fetch, id) =>
  R.compose(
    Future.cache,
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(`api/private/energiatodistukset/signed?id=${id}`)
);
