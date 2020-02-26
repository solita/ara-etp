import * as R from 'ramda';
import * as Future from './future-utils';

export const responseAsJson = Future.encaseP(response => response.json());

export const rejectWithInvalidResponse = R.ifElse(
  R.prop('ok'),
  Future.resolve,
  R.compose(Future.reject, R.prop('status'))
);

export const fetchFromUrl = R.curry((fetch, url) =>
  R.compose(
    R.chain(responseAsJson),
    R.chain(rejectWithInvalidResponse),
    Future.encaseP(fetch)
  )(url)
);

export const getFetch = url =>
  fetch(url, { headers: { Accept: 'application/json' } });

export const putFetch = R.curry((fetch, method, body, url) =>
  fetch(url, {
    method,
    body: JSON.stringify(body),
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    }
  })
);
