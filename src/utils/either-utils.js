import monet from 'monet';
import * as R from 'ramda';

export const { Either } = monet;

export const { of, Left, Right } = Either;

export const fold = R.curry((leftFn, rightFn, e) => e.fold(leftFn, rightFn));

export const foldRight = R.curry((initialValue, rightFn, e) =>
  e.foldRight(initialValue)(rightFn)
);

export const cata = R.curry((leftFn, rigthFn, either) =>
  either.cata(leftFn, rigthFn));

export const toMaybe = e => e.toMaybe();

export const map = R.curry((fn, e) => e.map(fn));

export const leftMap = R.curry((fn, e) => e.leftMap(fn));

export const isLeft = e => e.isLeft();

export const isRight = e => e.isRight();

export const right = e => e.right();

export const orSome = R.curry((defaultValue, either) =>
  either.cata(R.always(defaultValue), R.identity)
);

export const from = (value, isRight, left) =>
  isRight(value) ? Either.of(value) : Either.Left(left);

export const isEither = any => Object.getPrototypeOf(any) === Either.prototype;

export const fromValueOrEither = R.unless(isEither, Either.of);
