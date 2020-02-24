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
