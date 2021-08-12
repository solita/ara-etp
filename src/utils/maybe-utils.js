/**
 * @module Maybe
 * @description Light wrapper for Maybe type <br> {@link https://github.com/monet/monet.js/blob/master/docs/MAYBE.md}
 */

import monet from 'monet';
import * as R from 'ramda';

export const { Maybe } = monet;
export const {
  of,
  Some,
  None,
  fromNull,
  fromUndefined,
  fromEmpty,
  fromEither,
  fromFalsy
} = Maybe;

/**
 * @sig b -> (a -> b) -> Maybe a -> b
 */
export const fold = R.curry((defaultValue, fn, m) => m.fold(defaultValue)(fn));

/**
 * @sig (() -> b) -> (a -> b) -> Maybe a -> b
 */
export const cata = R.curry((noneFn, someFn, m) => m.cata(noneFn, someFn));

/**
 * @sig (a -> b) -> Maybe a -> Maybe b
 */
export const map = R.curry((fn, maybe) => maybe.map(fn));

/**
 * @sig (a -> Maybe b) -> Maybe a -> Maybe b
 */
export const chain = R.curry((fn, maybe) => maybe.chain(fn));

/**
 * @sig a -> Maybe a -> a
 */
export const getOrElse = R.curry((defaultValue, m) =>
  m.fold(defaultValue)(R.identity)
);

/**
 * @sig Maybe a -> Maybe a -> Maybe a
 */
export const orElse = R.curry((defaultValue, m) => m.orElse(defaultValue));

/**
 * @sig a -> Maybe a -> a
 */
export const orSome = R.curry((defaultValue, m) => m.orSome(defaultValue));

/**
 * @sig Maybe a -> a
 */
export const get = m => m.some();

/**
 * @sig Array a -> Maybe a
 */
export const head = R.compose(fromUndefined, R.head);

/**
 * @sig a -> Maybe b -> Either [a,b]
 */
export const toEither = R.curry((defaultValue, m) => m.toEither(defaultValue));

/**
 * @sig Maybe a -> Array a
 */
export const toArray = m => m.toArray();

/**
 * @sig (a -> boolean) -> Maybe a -> boolean
 */
export const exists = R.curry((pred, m) => m.exists(pred));

/**
 * @sig Maybe a -> boolean
 */
export const isSome = m => m.isSome();

/**
 * @sig Maybe a -> boolean
 */
export const isNone = m => m.isNone();

/**
 * @sig (() -> ()) -> Maybe a -> ()
 */
export const orElseRun = R.curry((fn, m) => m.orElseRun(fn));

/**
 * @sig a -> boolean
 */
export const isMaybe = any => Object.getPrototypeOf(any) === Maybe.prototype;

/**
 * @sig (a -> boolean) -> Array a -> Maybe a
 */
export const find = R.compose(Maybe.fromNull, R.find);

/**
 * @sig {string|number} -> Array b -> Maybe b
 */
export const findById = R.useWith(find, [R.propEq('id'), R.identity]);

/**
 * @sig Array [Maybe *] -> Maybe [Array [*]]
 */
export const toMaybeList = R.compose(
  map(list => list.toArray()),
  list => list.sequenceMaybe(),
  monet.List.fromArray
);

/**
 * @sig (a -> b) -> a -> Maybe b
 */
export const nullReturning = fn => R.compose(Maybe.fromNull, fn);
