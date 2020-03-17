import monet from 'monet';
import * as R from 'ramda';

export const { Either } = monet;

export const { of, Left, Right } = Either;

export const fold = R.curry((leftFn, rightFn, e) => e.fold(leftFn, rightFn));

export const foldRight = R.curry((initialValue, rightFn, e) =>
  e.foldRight(initialValue)(rightFn)
);

export const leftMap = R.curry((fn, e) => e.leftMap(fn));

export const isLeft = e => e.isLeft();

export const isRight = e => e.isRight();

export const right = e => e.right();

export const orSome = (either, defaultValue) =>
  either.cata(R.always(defaultValue), R.identity);

export const from = (value, isRight, left) =>
  isRight(value) ? Either.of(value) : Either.Left(left);

export const isEither = any => any.__proto__ === Either.prototype;

export const fromValueOrEither = any => isEither(any) ? any : Either.of(any);
