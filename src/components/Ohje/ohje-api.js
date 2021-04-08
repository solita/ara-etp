import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';

const url = {
  sivut: '/api/private/sivut',
  sivu: id => `${url.sivut}/${id}`
};

export const serialize = R.evolve({
  'parent-id': Maybe.orSome(null)
});

export const deserialize = R.evolve({
  'parent-id': Maybe.fromNull
});

export const getSivu = R.compose(
  R.map(deserialize),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch)),
  url.sivu
);

export const getSivut = R.compose(
  R.map(R.map(deserialize)),
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)(url.sivut);

export const postOhje = fetch =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.sivut)),
    serialize
  );
