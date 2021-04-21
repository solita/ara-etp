import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as yritysApi from '@Component/Yritys/yritys-api';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as kayttajat from '@Utility/kayttajat';
import * as dfns from 'date-fns';

export const url = {
  laatijat: '/api/private/laatijat',
  laatija: id => `${url.laatijat}/${id}`,
  yritykset: id => `${url.laatija(id)}/yritykset`
};

export const laatijaUploadSerialize = R.evolve({
  etunimi: Either.right,
  sukunimi: Either.right,
  henkilotunnus: Either.right,
  jakeluosoite: Either.right,
  postinumero: Either.right,
  postitoimipaikka: Either.right,
  email: Either.right,
  puhelin: Either.right,
  patevyystaso: Either.right,
  toteamispaivamaara: R.compose(
    Either.right,
    R.map(date => dfns.format(date, 'yyyy-MM-dd'))
  ),
  toteaja: Either.right,
  maa: Either.right
});

export const serialize = R.compose(
  R.evolve({
    'vastaanottajan-tarkenne': Maybe.orSome(null),
    henkilotunnus: Maybe.orSome(null),
    maa: Either.right,
    toimintaalue: Maybe.orSome(null),
    wwwosoite: Maybe.orSome(null),
    'api-key': Maybe.orSome(null)
  }),
  R.omit([
    'id',
    'login',
    'rooli',
    'passivoitu',
    'voimassaolo-paattymisaika',
    'voimassa'
  ])
);

export const serializeForLaatija = R.compose(
  R.omit([
    'etunimi',
    'sukunimi',
    'henkilotunnus',
    'patevyystaso',
    'toteamispaivamaara',
    'toteaja',
    'laatimiskielto'
  ]),
  serialize
);

export const putLaatijaById = R.curry((rooli, fetch, id, laatija) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.laatija(id))),
    R.ifElse(
      kayttajat.isPaakayttajaRole,
      R.always(serialize),
      R.always(serializeForLaatija)
    )(rooli)
  )(laatija)
);

export const getYritykset = R.curry((fetch, id) =>
  R.compose(
    R.map(R.map(R.evolve({ modifytime: dfns.parseJSON }))),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.yritykset
  )(id)
);

export const laatijat = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(R.__, url.laatijat))
)(fetch);

export const yritykset = R.compose(
  R.map(R.map(R.evolve({ modifytime: dfns.parseJSON }))),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  url.yritykset
);

const toggleLaatijaYritys = R.curry((method, fetch, laatijaId, yritysId) =>
  R.chain(
    Fetch.rejectWithInvalidResponse,
    Future.attemptP(_ =>
      fetch(url.yritykset(laatijaId) + '/' + yritysId, { method })
    )
  )
);

export const putLaatijaYritys = toggleLaatijaYritys('put');
export const deleteLaatijaYritys = toggleLaatijaYritys('delete');

export const patevyydet = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)('api/private/patevyydet');

export const uploadLaatijat = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.laatijat)),
  R.map(laatijaUploadSerialize)
);
