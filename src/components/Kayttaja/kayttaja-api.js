import * as R from 'ramda';

import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Maybe from '@Utility/maybe-utils';

import * as LaatijaUtils from '@Component/Laatija/laatija-utils';
import * as kayttajat from '@Utility/kayttajat';

const deserialize = R.evolve({
  login: R.compose(R.map(Date.parse), Maybe.fromNull),
  cognitoid: Maybe.fromNull,
  henkilotunnus: Maybe.fromNull,
  virtu: { localid: Maybe.fromNull, organisaatio: Maybe.fromNull }
});

export const url = {
  all: 'api/private/kayttajat',
  id: id => `${url.all}/${id}`,
  laatija: id => `${url.id(id)}/laatija`,
  whoami: '/api/private/whoami'
};

export const whoami = Future.cache(Fetch.fetchUrl(fetch, url.whoami));

export const getKayttajaById = R.curry((fetch, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.id
  )(id)
);

export const getLaatijaById = R.curry((fetch, id) =>
  R.compose(
    Future.chainRej(
      R.ifElse(R.equals(404), R.always(Future.resolve(null)), Future.reject)
    ),
    R.map(LaatijaUtils.deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.laatija
  )(id)
);

export const serialize = R.compose(
  R.evolve({
    henkilotunnus: Maybe.getOrElse(null),
    virtu: {localid: Maybe.getOrElse(null),
            organisaatio: Maybe.getOrElse(null)}
  }),
  R.omit(['id', 'login', 'cognitoid', 'ensitallennus'])
);

export const serializeForNonAdmin = R.compose(
  R.omit(['rooli', 'passivoitu', 'henkilotunnus', 'virtu']),
  serialize
);

export const putKayttajaById = R.curry((rooli, fetch, id, laatija) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.id(id))),
    R.ifElse(
      kayttajat.isPaakayttaja,
      R.always(serialize),
      R.always(serializeForNonAdmin)
    )(rooli)
  )(laatija)
);
