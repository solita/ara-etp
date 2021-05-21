import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Query from '@Utility/query';

import * as dfns from 'date-fns';

const url = {
  ketjut: '/api/private/viestit',
  ketjutCount: '/api/private/viestit/count',
  ketjutUnread: '/api/private/viestit/count/unread',
  ketju: id => `${url.ketjut}/${id}`,
  viestit: id => `${url.ketju(id)}/viestit`,
  kasittelijat: '/api/private/kasittelijat'
};

export const serialize = R.evolve({
  'kayttajarooli-id': Maybe.orSome(null),
  'energiatodistus-id': Maybe.orSome(null),
  'vastaanottajaryhma-id': Maybe.orSome(null)
});

export const deserialize = R.evolve({
  'kayttajarooli-id': Maybe.fromNull,
  viestit: R.map(
    R.evolve({
      'sent-time': dfns.parseJSON,
      'read-time': R.compose(R.map(dfns.parseJSON), Maybe.fromNull)
    })
  )
});

export const ketju = R.compose(
  R.map(deserialize),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  url.ketju
);

export const getKetjut = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  R.concat(url.ketjut),
  Query.toQueryString
);

export const getKetjutCount = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(url.ketjutCount);

export const getKetjutUnread = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(url.ketjutUnread);

export const getKasittelijat = R.compose(
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(url.kasittelijat);

export const postKetju = fetch =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.ketjut)),
    serialize
  );

export const putKetju = R.curry((fetch, id, body) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.ketju(id)))
  )(body)
);

export const postNewViesti = R.curry((fetch, id, body) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.viestit(id)))
  )(body)
);

export const vastaanottajaryhmat = Fetch.cached(fetch, '/vastaanottajaryhmat');
