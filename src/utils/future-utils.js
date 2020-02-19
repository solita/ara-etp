import * as Fluture from 'fluture';
import * as R from 'ramda';

export { Fluture };

export const { of, resolve, reject, encaseP, promise } = Fluture;

export const fork = R.curry((leftFn, rightFn, future) =>
  Fluture.fork(leftFn)(rightFn)(future)
);

export const coalesce = R.curry((leftFn, rightFn, f) =>
  Fluture.coalesce(leftFn)(rightFn)(f)
);
