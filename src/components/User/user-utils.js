import * as R from 'ramda';
import * as Future from '../../utils/future-utils';

const userApi = `/api/users/`;

export const urlForUserId = id => `${userApi}${id}`;

export const responseAsJson = Future.encaseP(response => response.json());

export const rejectWithInvalidResponse = R.ifElse(
  R.prop('ok'),
  Future.resolve,
  R.compose(Future.reject)
);

export const userFuture = R.curry((fetch, id) =>
  R.compose(
    R.chain(responseAsJson),
    R.chain(rejectWithInvalidResponse),
    Future.encaseP(fetch),
    urlForUserId
  )(id)
);
