import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Parsers from '@Utility/parsers';
import qs from 'qs';

export const toQueryString = R.compose(
  Maybe.fold('', R.concat('?')),
  Parsers.optionalString,
  qs.stringify,
  R.map(Maybe.get),
  R.filter(Maybe.isSome)
);

export const parseBoolean = R.compose(
  R.map(R.equals('true')),
  Parsers.optionalString);