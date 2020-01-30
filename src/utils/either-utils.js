import monet from 'monet';
import * as R from 'ramda';

export const { Either } = monet;

export const { of, Left, Right } = Either;

export const fold = R.curry((leftFn, rightFn, e) => e.fold(leftFn, rightFn));
