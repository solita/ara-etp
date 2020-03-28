import * as R from "ramda";
import * as Fetch from "@Utility/fetch-utils";
import * as Future from "@Utility/future-utils";
import * as Either from "@Utility/either-utils";
import * as Maybe from "@Utility/maybe-utils";

export const deserialize = R.evolve({
  nimi: Maybe.fromNull,
});

export const serialize = R.compose(
  R.evolve({
    nimi: Maybe.getOrElse(null)
  }),
  R.dissoc('id')
);

export const url = {
  energiatodistukset: '/api/private/energiatodistukset',
  energiatodistus: id => `${url.energiatodistukset}/${id}`,
};

export const getEnergiatodistusById = R.curry((fetch, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.energiatodistus
  )(id)
);

export const putEnergiatodistusById = R.curry((fetch, id, energiatodistus) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.energiatodistus(id))),
    serialize
  )(energiatodistus)
);

export const postEnergiatodistus = R.curry((fetch, energiatodistus) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.energiatodistukset)),
    serialize
  )(energiatodistus)
);