import * as R from 'ramda';
import * as Future from './future-utils';

export const toJson = Future.encaseP(response => response.json());

export const rejectWithInvalidResponse = R.ifElse(
  R.prop('ok'),
  Future.resolve,
  R.compose(Future.reject, R.prop('status'))
);

export const responseAsJson = R.compose(
  R.chain(toJson),
  R.chain(rejectWithInvalidResponse)
);

export const fetchFromUrl = R.curry((fetch, url) =>
  R.compose(R.map(responseAsJson), Future.encaseP(fetch))(url)
);

export const getFetch = url =>
  fetch(url, { headers: { Accept: 'application/json' } });

export const fetchWithMethod = R.curry((method, url, body) =>
  fetch(url, {
    method,
    body: JSON.stringify(body),
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    }
  })
);
