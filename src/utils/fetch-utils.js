/**
 * @module Fetch
 * @description Utilities for handling generic fetch related actions
 */

/**
 * @typedef {Object} ErrorResponse
 * @property {Object|string} body
 * @property {number} status
 * @property {string} url
 * @property {string?} contentType
 */

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

/**
 * @private
 * @sig Response -> Future [*,ErrorResponse]
 */
const invalidResponse = response =>
  R.compose(
    R.map(R.assoc('contentType', contentType(response))),
    R.map(R.mergeLeft(R.pick(['status', 'url'], response))),
    Future.coalesce(R.always({}), R.objOf('body')),
    R.ifElse(isJson, toJson, toText)
  )(response);

/**
 * @sig Response -> Future [ErrorResponse,Response]
 */
export const rejectWithInvalidResponse = R.ifElse(
  R.prop('ok'),
  Future.resolve,
  R.compose(R.chain(Future.reject), invalidResponse)
);

export const responseAsJson = R.compose(
  R.chain(toJson),
  R.chain(rejectWithInvalidResponse)
);

/**
 * @sig Future [ErrorResponse,Response] -> Future [ErrorResponse,Blob]
 */
export const responseAsBlob = R.compose(
  R.chain(toBlob),
  R.chain(rejectWithInvalidResponse)
);

/**
 * @sig Future [ErrorResponse,Response] -> Future [ErrorResponse,JSON]
 */
export const responseAsText = R.compose(
  R.chain(toText),
  R.chain(rejectWithInvalidResponse)
);

/**
 * @sig Fetch -> string -> Promise
 */
export const getFetch = R.curry((fetch, url) =>
  fetch(url, { headers: { Accept: 'application/json' } })
);

/**
 * @sig Fetch -> string -> Future [Response,JSON]
 */
export const getJson = R.curry((fetch, url) =>
  R.compose(responseAsJson, Future.encaseP(getFetch(fetch)))(url)
);

/**
 * @sig Fetch -> string -> string -> Object|string -> Promise
 */
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

/**
 * @sig Fetch -> string -> Promise
 */
export const postEmpty = R.curry((fetch, url) =>
  fetch(url, { method: 'post', headers: { Accept: 'application/json' } })
);

/**
 * @sig Fetch -> string -> Promise
 */
export const deleteRequest = R.curry((fetch, url) =>
  fetch(url, { method: 'delete' })
);

export const deleteFuture = R.compose(
  R.chain(rejectWithInvalidResponse),
  Future.encaseP(deleteRequest(fetch)));

/**
 * @sig Fetch -> string -> Future [ErrorResponse, Response]
 * @description Cached future to fetch static data from backend
 */
export const cached = R.curry((fetch, url) =>
  R.compose(Future.cache, getJson)(fetch, api + url)
);
