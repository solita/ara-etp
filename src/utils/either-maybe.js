import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';


export const toMaybe = R.compose(
  R.chain(R.identity),
  Either.toMaybe);

export const orSome = R.curry((defaultValue, em) => R.compose(
  Maybe.orSome(defaultValue),
  toMaybe
)(em))