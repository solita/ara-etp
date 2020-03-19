import * as Fluture from 'fluture';
import * as R from 'ramda';

export { Fluture };

export const { resolve, reject, encaseP, promise, attemptP } = Fluture;

export const fork = R.curry((leftFn, rightFn, future) =>
  Fluture.fork(leftFn)(rightFn)(future)
);

export const forkBothDiscardFirst = R.curry((leftFn, rightFn, future) =>
  Fluture.fork(R.compose(leftFn, R.last))(R.compose(rightFn, R.last))(future)
);

export const coalesce = R.curry((leftFn, rightFn, f) =>
  Fluture.coalesce(leftFn)(rightFn)(f)
);

export const both = R.curry((firstFuture, secondFuture) =>
  Fluture.both(firstFuture)(secondFuture)
);

export const after = R.curry((delay, value) => Fluture.after(delay)(value));

export const value = R.curry((fn, f) => Fluture.value(fn)(f));
