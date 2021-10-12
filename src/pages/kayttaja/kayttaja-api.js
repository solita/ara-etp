import * as R from 'ramda';

import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Kayttajat from '@Utility/kayttajat';
import * as Parsers from '@Utility/parsers';

export const deserialize = R.evolve({
  login: R.compose(
    R.chain(Either.toMaybe),
    R.map(Parsers.parseISODate),
    Maybe.fromNull
  ),
  cognitoid: Maybe.fromNull,
  henkilotunnus: Maybe.fromNull,
  virtu: Maybe.fromNull
});

export const deserializeLaatija = R.compose(
  R.assoc('api-key', Maybe.None()),
  R.evolve({
    'vastaanottajan-tarkenne': Maybe.fromNull,
    maa: Either.Right,
    toimintaalue: Maybe.fromNull,
    wwwosoite: Maybe.fromNull,

    // we assume that these dates are always valid from backend
    'toteamispaivamaara': R.compose(Either.right, Parsers.parseISODate),
    'voimassaolo-paattymisaika': R.compose(Either.right, Parsers.parseISODate)
  })
);

export const url = {
  all: 'api/private/kayttajat',
  id: id => `${url.all}/${id}`,
  laatija: id => `${url.id(id)}/laatija`,
  whoami: '/api/private/whoami'
};

export const whoami = Future.cache(Fetch.getJson(fetch, url.whoami));

export const getKayttajaById = R.compose(
  R.map(deserialize),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  url.id
);

export const kayttajat = R.map(
  R.map(deserialize),
  Fetch.getJson(fetch, url.all)
);

export const getLaatijaById = R.curry((fetch, id) =>
  R.compose(
    R.map(deserializeLaatija),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.laatija
  )(id)
);

export const serialize = R.compose(
  R.evolve({
    henkilotunnus: Maybe.orSome(null),
    virtu: Maybe.orSome(null)
  }),
  R.omit(['id', 'login', 'cognitoid', 'ensitallennus'])
);

export const serializeForNonAdmin = R.compose(
  R.omit(['rooli', 'passivoitu', 'henkilotunnus', 'virtu']),
  serialize
);

export const putKayttajaById = R.curry((rooli, fetch, id, kayttaja) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.id(id))),
    R.ifElse(
      Kayttajat.isPaakayttajaRole,
      R.always(serialize),
      R.always(serializeForNonAdmin)
    )(rooli)
  )(kayttaja)
);

export const postKayttaja = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.all)),
  serialize
);

export const roolit = Fetch.cached(fetch, '/roolit');
