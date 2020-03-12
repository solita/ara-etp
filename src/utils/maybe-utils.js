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
  map
} = Maybe;

export const fold = R.curry((defaultValue, fn, m) => m.fold(defaultValue)(fn));

export const cata = R.curry((noneFn, someFn, m) => m.cata(noneFn, someFn));

export const getOrElse = R.curry((defaultValue, m) =>
  m.fold(defaultValue)(R.identity)
);

export const orElse = R.curry((defaultValue, m) => m.orElse(defaultValue));

export const head = R.compose(fromUndefined, R.head);

export const toEither = R.curry((defaultValue, m) => m.toEither(defaultValue));
