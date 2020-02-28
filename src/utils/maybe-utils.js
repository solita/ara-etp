import monet from 'monet';
import * as R from 'ramda';

export const { Maybe } = monet;
export const { of, Some, None, fromNull, fromEmpty, fromEither } = Maybe;

export const fold = R.curry((defaultValue, fn, m) => m.fold(defaultValue)(fn));

export const cata = R.curry((noneFn, someFn, m) => m.cata(noneFn, someFn));

export const getOrElse = R.curry((defaultValue, m) =>
  m.fold(defaultValue)(R.identity)
);
