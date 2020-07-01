import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as yritysApi from '@Component/Yritys/yritys-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as kayttajat from '@Utility/kayttajat';

export const url = {
  laatijat: '/api/private/laatijat',
  laatija: id => `${url.laatijat}/${id}`,
  yritykset: id => `${url.laatija(id)}/yritykset`
};

export const serialize = R.compose(
  R.evolve({
    'vastaanottajan-tarkenne': Maybe.orSome(null),
    maa: Either.right,
    toimintaalue: Maybe.orSome(null),
    wwwosoite: Maybe.getOrElse(null)
  }),
  R.omit(['id', 'email', 'login', 'rooli', 'passivoitu'])
);

export const serializeForLaatija = R.compose(
  R.omit(['patevyystaso', 'toteamispaivamaara', 'toteaja', 'laatimiskielto']),
  serialize
);

export const putLaatijaById = R.curry((rooli, fetch, id, laatija) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.laatija(id))),
    R.ifElse(
      kayttajat.isPaakayttaja,
      R.always(serialize),
      R.always(serializeForLaatija)
    )(rooli)
  )(laatija)
);

export const getYritykset = R.curry((fetch, id) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.yritykset
  )(id)
);

export const getLaatijat = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(R.__, url.laatijat))
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

export const getAllYritykset = yritysApi.getAllYrityksetFuture;

export const patevyydet = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)('api/private/patevyydet/');
