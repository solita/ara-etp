import * as R from 'ramda';
import * as Future from './future-utils';
import * as Maybe from './maybe-utils';

const api = 'api/private';

export const toJson = Future.encaseP(response => response.json());

export const toText = Future.encaseP(response => response.text());

export const toBlob = Future.encaseP(response => response.blob());

const contentType = R.compose(
  R.chain(headers => Maybe.fromNull(headers.get('content-type'))),
  Maybe.fromNull,
  R.prop('headers')
);

const isJson = R.compose(
  Maybe.exists(R.startsWith('application/json')),
  contentType
);

const invalidResponse = response =>
  R.compose(
    R.map(R.assoc('contentType', contentType(response))),
    R.map(R.mergeLeft(R.pick(['status', 'url'], response))),
    Future.coalesce(R.always({}), R.objOf('body')),
    R.ifElse(isJson, toJson, toText)
  )(response);

export const rejectWithInvalidResponse = R.ifElse(
  R.prop('ok'),
  Future.resolve,
  R.compose(R.chain(Future.reject), invalidResponse)
);

export const responseAsJson = R.compose(
  R.chain(toJson),
  R.chain(rejectWithInvalidResponse)
);

export const responseAsBlob = R.compose(
  R.chain(toBlob),
  R.chain(rejectWithInvalidResponse)
);

export const responseAsText = R.compose(
  R.chain(toText),
  R.chain(rejectWithInvalidResponse)
);

export const getFetch = R.curry((fetch, url) =>
  fetch(url, { headers: { Accept: 'application/json' } })
);

export const getJson = R.curry((fetch, url) =>
  R.compose(responseAsJson, Future.encaseP(getFetch(fetch)))(url)
);

export const fetchWithMethod = R.curry((fetch, method, url, body) =>
  fetch(url, {
    method,
    body: JSON.stringify(body),
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    }
  })
);

export const postEmpty = R.curry((fetch, url) =>
  fetch(url, { method: 'post', headers: { Accept: 'application/json' } })
);

export const deleteRequest = R.curry((fetch, url) =>
  fetch(url, { method: 'delete' })
);

/**
 * Cached future to fetch static data from backend
 * @type {any}
 */
export const cached = R.curry((fetch, url) =>
  R.compose(Future.cache, getJson)(fetch, api + url)
);
