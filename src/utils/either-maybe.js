/**
 * @module EM
 * @description Utilities for handling Maybe values wrapped in Eithers
 */

import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

/**
 * @sig a -> Either [Maybe a]
 */
export const fromNull = R.compose(Either.Right, Maybe.fromNull);

/**
 * @sig Either [Maybe a] -> Maybe a
 */
export const toMaybe = R.compose(R.chain(R.identity), Either.toMaybe);

/**
 * @sig a -> Either [Maybe a] -> a
 */
export const orSome = R.curry((defaultValue, em) =>
  R.compose(Maybe.orSome(defaultValue), toMaybe)(em)
);

/**
 * @sig a -> (b -> a) -> Either [Maybe b]
 */
export const fold = R.curry((defaultValue, fn, em) =>
  R.compose(Maybe.orSome(defaultValue), R.map(fn), toMaybe)(em)
);

/**
 * @sig (a -> (), Either [Maybe a] -> ())
 */
export const forEach = (fn, em) => toMaybe(em).forEach(fn);

/**
 * @sig Either [Maybe a] -> Array [a]
 */
export const toArray = R.compose(Maybe.toArray, toMaybe);

/**
 * (a -> boolean) -> Either [Maybe a] -> boolean
 */
export const exists = R.curry((pred, em) => fold(false, pred, em));
