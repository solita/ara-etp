import monet from 'monet';
import * as R from 'ramda';

export const { Either } = monet;

export const { of, Left, Right } = Either;

export const fold = R.curry((leftFn, rightFn, e) => e.fold(leftFn, rightFn));

export const foldRight = R.curry((initialValue, rightFn, e) =>
  e.foldRight(initialValue)(rightFn)
);

export const isLeft = e => e.isLeft();

export const isRight = e => e.isRight();

export const fromValueOrEither =
    any => any['@@type'] === Either.prototype.init['@@type'] ?
      any : Either.of(any);