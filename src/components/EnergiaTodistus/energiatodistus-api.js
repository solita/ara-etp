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
  deep.map(Maybe.isMaybe, R.ifElse(Maybe.isMaybe, Maybe.orSome(null), R.identity)),
  R.dissoc('id')
);

export const url = {
  all: '/api/private/energiatodistukset',
  year: (year) => `${url.all}/${year}`,
  id: (year, id) => `${url.year(year)}/${id}`,
};

export const getEnergiatodistusById = R.curry((fetch, year, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.id
  )(year, id)
);

export const putEnergiatodistusById = R.curry((fetch, year, id, energiatodistus) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.id(year, id))),
    serialize
  )(energiatodistus)
);

export const postEnergiatodistus = R.curry((fetch, year, energiatodistus) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.year(year))),
    serialize
  )(energiatodistus)
);