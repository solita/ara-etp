import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';

import * as dfns from 'date-fns';

const url = {
  ketjut: '/api/private/viestit',
  ketju: id => `${url.ketjut}/${id}`,
  viestit: id => `${url.ketju(id)}/viestit`
};

export const serialize = R.evolve({
  'kayttajarooli-id': Maybe.orSome(null),
  'kayttajaryhma-id': Maybe.orSome(null),
  'energiatodistus-id': Maybe.orSome(null)
});

export const deserialize = R.evolve({
  'kayttajarooli-id': Maybe.fromNull,
  'kayttajaryhma-id': Maybe.fromNull,
  viestit: R.map(R.evolve({ senttime: dfns.parseJSON }))
});

export const getKetju = fetch =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.ketju
  );

export const getKetjut = fetch =>
  R.compose(
    R.map(R.map(deserialize)),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(url.ketjut);

export const postKetju = fetch =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.ketjut)),
    serialize
  );

export const postNewViesti = R.curry((fetch, id, body) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.viestit(id)))
  )(body)
);
