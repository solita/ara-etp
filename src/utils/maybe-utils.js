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
  fromEither
} = Maybe;

export const fold = R.curry((defaultValue, fn, m) => m.fold(defaultValue)(fn));

export const cata = R.curry((noneFn, someFn, m) => m.cata(noneFn, someFn));

export const map = R.curry((fn, maybe) => maybe.map(fn));

export const getOrElse = R.curry((defaultValue, m) =>
  m.fold(defaultValue)(R.identity)
);

export const orElse = R.curry((defaultValue, m) => m.orElse(defaultValue));

export const orSome = R.curry((defaultValue, m) => m.orSome(defaultValue));

export const get = m => m.some();

export const head = R.compose(fromUndefined, R.head);

export const toEither = R.curry((defaultValue, m) => m.toEither(defaultValue));

export const isSome = m => m.isSome();

export const isNone = m => m.isNone();

export const orElseRun = R.curry((fn, m) => m.orElseRun(fn));

export const isMaybe = any => Object.getPrototypeOf(any) === Maybe.prototype;

export const findById = R.curry((id, collection) =>
  R.compose(Maybe.fromNull, R.find(R.propEq('id', id)))(collection)
);
