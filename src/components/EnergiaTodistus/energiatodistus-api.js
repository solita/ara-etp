import * as R from "ramda";
import * as Fetch from "@Utility/fetch-utils";
import * as Future from "@Utility/future-utils";
import * as Either from "@Utility/either-utils";
import * as Maybe from "@Utility/maybe-utils";
import * as deep from '@Utility/deep-objects';

export const deserialize = R.evolve({
  perustiedot: {
    nimi: Maybe.fromNull,
  }
});

export const serialize = R.compose(
  deep.map(Maybe.isMaybe,
    R.ifElse(R.allPass([R.complement(R.isNil), Maybe.isMaybe]), Maybe.orSome(null), R.identity)),
  R.omit(['id', 'tila'])
);

export const url = {
  all: '/api/private/energiatodistukset',
  version: (version) => `${url.all}/${version}`,
  id: (version, id) => `${url.version(version)}/${id}`,
};

export const getEnergiatodistusById = R.curry((fetch, version, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.id
  )(version, id)
);

export const putEnergiatodistusById = R.curry((fetch, version, id, energiatodistus) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.id(version, id))),
    serialize
  )(energiatodistus)
);

export const postEnergiatodistus = R.curry((fetch, version, energiatodistus) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.version(version))),
    serialize
  )(energiatodistus)
);