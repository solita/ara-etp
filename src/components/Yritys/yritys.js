import * as R from 'ramda';
import * as Future from '../../utils/future-utils';
import * as YritysUtils from './yritys-utils';

export const fetchYritys = R.compose(
  Future.promise,
  YritysUtils.yritysFetchFuture(fetch)
);
