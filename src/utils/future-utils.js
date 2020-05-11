import * as Fluture from 'fluture';
import * as R from 'ramda';

export { Fluture };

export const { resolve, reject, encaseP, promise, attemptP, cache } = Fluture;

export const chainRej = R.curry((mapper, future) =>
  Fluture.chainRej(mapper)(future)
);

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

export const parallel = R.curry((concurrency, futures) =>
  Fluture.parallel(concurrency)(futures)
);

export const parallelObject = R.curry((concurrency, futures) =>
  Fluture.map(values => R.zipObj(R.keys(futures), values))(
    Fluture.parallel(concurrency)(R.values(futures))));

export const after = R.curry((delay, value) => Fluture.after(delay)(value));

export const value = R.curry((fn, f) => Fluture.value(fn)(f));

export const delay = R.curry((amount, future) =>
  R.compose(R.map(R.last), both(after(amount, true)))(future)
);

export const filter = R.curry((predicate, rejectValue, f) =>
  R.chain(
    value =>
      predicate(value) ? Fluture.resolve(value) : Fluture.reject(rejectValue),
    f
  )
);
