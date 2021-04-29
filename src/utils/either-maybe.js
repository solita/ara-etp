import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

export const fromNull = R.compose(Either.Right, Maybe.fromNull);

export const toMaybe = R.compose(R.chain(R.identity), Either.toMaybe);

export const orSome = R.curry((defaultValue, em) =>
  R.compose(Maybe.orSome(defaultValue), toMaybe)(em)
);

export const fold = R.curry((defaultValue, fn, em) =>
  R.compose(Maybe.orSome(defaultValue), R.map(fn), toMaybe)(em)
);

export const forEach = (fn, em) => toMaybe(em).forEach(fn);

export const toArray = R.compose(Maybe.toArray, toMaybe)
