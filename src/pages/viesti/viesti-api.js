import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Query from '@Utility/query';

import * as dfns from 'date-fns';
import * as EtApi from '@Pages/energiatodistus/energiatodistus-api';

export const url = {
  ketjut: '/api/private/viestit',
  ketjutCount: '/api/private/viestit/count',
  ketjutUnread: '/api/private/viestit/count/unread',
  ketju: id => `${url.ketjut}/${id}`,
  viestit: id => `${url.ketju(id)}/viestit`,
  kasittelijat: '/api/private/kasittelijat',
  energiatodistusKetjut: id =>
    `/api/private/energiatodistukset/all/${id}/viestit`,
  liitteet: id => url.ketju(id) + '/liitteet'
};

export const serialize = R.evolve({
  'kayttajarooli-id': Maybe.orSome(null),
  'vastaanottajaryhma-id': Maybe.orSome(null),
  'energiatodistus-id': Maybe.orSome(null)
});

export const deserialize = R.evolve({
  'has-kasittelija': Maybe.fromNull,
  'include-kasitelty': Maybe.fromNull,
  'kayttajarooli-id': Maybe.fromNull,
  'kasittelija-id': Maybe.fromNull,
  'energiatodistus-id': Maybe.fromNull,
  'vo-toimenpide-id': Maybe.fromNull,
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
  Future.encaseP(Fetch.getFetch(fetch)),
  R.concat(url.ketjutCount),
  Query.toQueryString
);
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

export const getEnergiatodistusKetjut = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  url.energiatodistusKetjut
);

/* Liitteiden palvelut */

export const liitteet = R.compose(
  R.map(R.map(EtApi.deserializeLiite)),
  Fetch.getJson(fetch),
  url.liitteet
);

export const postLiitteetFiles = R.curry((viestiketjuId, files) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(files =>
      fetch(url.liitteet(viestiketjuId) + '/files', {
        method: 'POST',
        body: EtApi.toFormData('files', files)
      })
    )
  )(files)
);

export const postLiitteetLink = R.curry((viestiketjuId, link) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(
        fetch,
        'post',
        url.liitteet(viestiketjuId) + '/link'
      )
    )
  )(link)
);

export const deleteLiite = R.curry((viestiketjuId, liiteId) =>
  Fetch.deleteFuture(url.liitteet(viestiketjuId) + '/' + liiteId)
);
