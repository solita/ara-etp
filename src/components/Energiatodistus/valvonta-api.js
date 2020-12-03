import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';

export const url = {
  valvonta: '/api/private/valvonta',
  id: id => `${url.valvonta}/${id}`
}

export const getValvonta = R.curry((fetch, energiatodistusId) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.id
  )(energiatodistusId)
);

export const putValvonta = R.curry((fetch, energiatodistusId, isActive) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch,
      'put', url.id(energiatodistusId)))
  )({ active: isActive }));