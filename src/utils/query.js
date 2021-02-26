import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const toQueryString = R.compose(
  Maybe.orSome(''),
  R.map(R.compose(R.concat('?'), R.join('&'))),
  Maybe.toMaybeList,
  R.filter(Maybe.isSome),
  R.map(([key, optionalValue]) =>
    R.map(value => `${key}=${value}`, optionalValue)
  ),
  R.toPairs
);
