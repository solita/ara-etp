import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as deep from '@Utility/deep-objects';

export const deserialize = R.compose(
  R.evolve({
    id: Maybe.get,
    versio: Maybe.get,
    perustiedot: {
      'onko-julkinen-rakennus': Maybe.get,
      valmistumisvuosi: Either.Right
    }
  }),
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
  R.omit(['id', 'tila', 'laatija-fullname', 'versio'])
);

export const url = {
  all: '/api/private/energiatodistukset',
  version: version => `${url.all}/${version}`,
  id: (version, id) => `${url.version(version)}/${id}`,
  sign: (version, id) => `${url.id(version, id)}/digest`
};

export const getEnergiatodistukset = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(url.all);

export const getEnergiatodistusById = R.curry((fetch, version, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.id
  )(version, id)
);

export const putEnergiatodistusById = R.curry(
  (fetch, version, id, energiatodistus) =>
    R.compose(
      R.chain(Fetch.rejectWithInvalidResponse),
      Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.id(version, id))),
      serialize
    )(energiatodistus)
);

export const getEnergiatodistusDigestById = R.curry((fetch, version, id) =>
  R.compose(
    //Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.sign
  )(version, id)
);

export const postEnergiatodistus = R.curry((fetch, version, energiatodistus) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.version(version))),
    serialize
  )(energiatodistus)
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

export const kayttotarkoitusluokat2018 = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)('api/private/kayttotarkoitusluokat/2018');

export const alakayttotarkoitusluokat2018 = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)('api/private/alakayttotarkoitusluokat/2018');
