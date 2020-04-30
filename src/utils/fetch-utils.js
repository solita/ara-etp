import * as R from 'ramda';
import * as Future from './future-utils';

export const toJson = Future.encaseP(response => response.json());

export const toText = Future.encaseP(response => response.text());

export const rejectWithInvalidResponse = R.ifElse(
  R.prop('ok'),
  Future.resolve,
  R.compose(Future.reject, R.prop('status'))
);

export const responseAsJson = R.compose(
  R.chain(toJson),
  R.chain(rejectWithInvalidResponse)
);

export const responseAsText = R.compose(
  R.chain(toText),
  R.chain(rejectWithInvalidResponse)
);

export const fetchFromUrl = R.curry((fetch, url) =>
  R.compose(responseAsJson, Future.encaseP(fetch))(url)
);

export const getFetch = R.curry((fetch, url) =>
  fetch(url, { headers: { Accept: 'application/json' } })
);

export const fetchUrl = R.curry((fetch, url) =>
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
