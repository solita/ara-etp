import * as R from 'ramda';
import * as Fetch from '../../utils/fetch-utils';
import * as Maybe from '../../utils/maybe-utils';

const userApi = `/api/users/`;

export const urlForUserId = id => `${userApi}${id}`;

export const userFuture = R.curry((fetch, id) =>
  R.compose(Fetch.fetchFromUrl(fetch), urlForUserId)(id)
);
