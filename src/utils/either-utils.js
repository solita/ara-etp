/**
 * @module Either
 * @description Light wrapper for Either type <br> {@link https://github.com/monet/monet.js/blob/master/docs/EITHER.md}
 */

import monet from 'monet';
import * as R from 'ramda';

export const { Either } = monet;

export const { of, Left, Right } = Either;

/**
 * @sig (b -> c) -> (a -> c) -> Either (b,a) -> c
 */
export const fold = R.curry((leftFn, rightFn, e) => e.fold(leftFn, rightFn));

/**
 * @sig b -> (a -> b) -> Either [*,a] -> b
 */
export const foldRight = R.curry((initialValue, rightFn, e) =>
  e.foldRight(initialValue)(rightFn)
);

/**
 * @sig (b -> c) -> (a -> c) -> Either [b,a] -> c
 */
export const cata = R.curry((leftFn, rigthFn, either) =>
  either.cata(leftFn, rigthFn)
);

/**
 * @sig Either a -> Maybe a
 */
export const toMaybe = e => e.toMaybe();

/**
 * @sig (a -> b) -> Either [*,a] -> Either [*,b]
 */
export const map = R.curry((fn, e) => e.map(fn));

/**
 * @sig (a -> b) -> Either [a,*] -> Either [b,*]
 */
export const leftMap = R.curry((fn, e) => e.leftMap(fn));

/**
 * @sig Either [*,*] -> boolean
 */
export const isLeft = e => e.isLeft();

/**
 * @sig Either [*,*] -> boolean
 */
export const isRight = e => e.isRight();

/**
 * @sig Either [*,a] -> a
 */
export const right = e => e.right();

/**
 * @sig Either [a,*] -> a
 */
export const left = e => e.left();

/**
 * @sig a -> Either [*,a] -> a
 */
export const orSome = R.curry((defaultValue, either) =>
  either.cata(R.always(defaultValue), R.identity)
);

/**
 * @sig a -> (a -> boolean) -> b -> Either [b,a]
 */
export const from = (value, isRight, left) =>
  isRight(value) ? Either.of(value) : Either.Left(left);

/**
 * @sig (() -> a) -> Either [*, a]
 */
export const fromTry = fn => Either.fromTry(fn);

/**
 * @sig a -> boolean
 */
export const isEither = any =>
  R.complement(R.isNil)(any) && Object.getPrototypeOf(any) === Either.prototype;

/**
 * @sig (a | Either [*,a]) -> Either [*,a]
 */
export const fromValueOrEither = R.unless(isEither, Either.of);

/**
 * @sig b -> (a -> boolean) -> Either [b,a] -> Either [b,a]
 */
export const filter = R.curry((leftValue, filterFn, e) =>
  R.chain(R.ifElse(filterFn, Right, R.always(Left(leftValue))), e)
);

/**
 * @sig (a -> ()) -> Either [a,*] -> ()
 */
export const forEachLeft = R.curry((leftFn, e) => e.forEachLeft(leftFn));
