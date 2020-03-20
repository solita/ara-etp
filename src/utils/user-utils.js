import * as R from 'ramda';
import * as Future from './future-utils';
import * as Fetch from './fetch-utils';
import * as Error from './error-utils';
import { currentUserStore, errorStore } from '../stores';

const userApi = `/api/private/kayttajat/`;
const whoamiUrl = '/api/private/whoami'

export const urlForUserId = id => `${userApi}${id}`;

export const userFuture = R.curry((fetch, id) =>
  R.compose(Fetch.fetchFromUrl(fetch), urlForUserId)(id)
);

export const fetchAndStoreUser = () => {
  Future.fork(R.compose(
    errorStore.set,
    Error.httpError({})
  ),
  currentUserStore.set,
  Fetch.fetchFromUrl(fetch, whoamiUrl));
}
