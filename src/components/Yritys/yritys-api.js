import * as Future from '@Utility/future-utils';
import * as dfns from 'date-fns';
import * as geoApi from '@Component/Geo/geo-api';
import * as laskutusApi from '@Component/Laskutus/laskutus-api';
import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as EM from '@Utility/either-maybe';

/**
 * This namespace is for all flutures related to yritys api
 */

export const url = {
  yritykset: '/api/private/yritykset',
  yritys: id => `${url.yritykset}/${id}`,
  laatijat: id => `${url.yritykset}/${id}/laatijat`
};

export const deserialize = R.evolve({
  'vastaanottajan-tarkenne': Maybe.fromNull,
  maa: Either.Right,
  verkkolaskuosoite: Maybe.fromNull,
  verkkolaskuoperaattori: Maybe.fromNull
});

export const serialize = R.compose(
  R.evolve({
    'vastaanottajan-tarkenne': Maybe.orSome(null),
    maa: Either.right,
    verkkolaskuosoite: Maybe.orSome(null),
    verkkolaskuoperaattori: R.ifElse(
      Either.isEither,
      EM.orSome(null),
      Maybe.orSome(null)
    )
  }),
  R.dissoc('id')
);

export const luokittelut = Future.parallelObject(3, {
  laskutuskielet: laskutusApi.laskutuskielet,
  verkkolaskuoperaattorit: laskutusApi.verkkolaskuoperaattorit,
  countries: geoApi.countries
});

export const getAllYritykset = fetch =>
  R.compose(
    R.map(R.map(deserialize)),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(url.yritykset);

export const getYritysById = R.compose(
  R.map(deserialize),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  url.yritys
);

export const putYritysById = R.curry((fetch, id, yritys) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.yritys(id))),
    serialize
  )(yritys)
);

export const postYritys = R.curry((fetch, yritys) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.yritykset)),
    serialize
  )(yritys)
);

export const getLaatijatById = R.curry((fetch, id) =>
  R.compose(
    R.map(R.map(R.evolve({ modifytime: dfns.parseJSON }))),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.laatijat
  )(id)
);

export const putAcceptedLaatijaYritys = R.curry((fetch, laatijaId, yritysId) =>
  R.chain(
    Fetch.rejectWithInvalidResponse,
    Future.attemptP(_ =>
      fetch(url.laatijat(yritysId) + '/' + laatijaId, { method: 'put' })
    )
  )
);
