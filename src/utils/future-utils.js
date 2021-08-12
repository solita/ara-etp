/**
 * @module Future
 * @description Light wrapper for Future type
 */

/**
 * @typedef {Object} Future
 */

import * as Fluture from 'fluture';
import * as R from 'ramda';

export { Fluture };

export const {
  Future,
  resolve,
  reject,
  encaseP,
  promise,
  attemptP,
  cache
} = Fluture;

/**
 * @sig Future [a,c] -> Future [a,b] -> Future [a,c]
 * @description {@link https://github.com/fluture-js/Fluture#and}
 */
export const and = R.curry((firstFuture, secondFuture) =>
  Fluture.and(firstFuture)(secondFuture)
);

/**
 * @sig Future [a,c] -> Future [a,b] -> Future [a,b]
 * @description {@link https://github.com/fluture-js/Fluture#lastly}
 */
export const lastly = R.curry((cleanup, action) =>
  Fluture.lastly(cleanup)(action)
);

/**
 * @sig (a -> Future [c,b]) -> Future [a,b] -> Future [c,b]
 * @description {@link https://github.com/fluture-js/Fluture#chain}
 */
export const chain = R.curry((mapper, future) => Fluture.chain(mapper)(future));

/**
 * @sig (c -> Future [b,c]) -> Future [a,c] -> Future [b,c]
 * @description {@link https://github.com/fluture-js/Fluture#chainRej}
 */
export const chainRej = R.curry((mapper, future) =>
  Fluture.chainRej(mapper)(future)
);

/**
 * @sig (a -> ()) -> (b -> ()) -> Future [a,b] -> Function
 * @description {@link https://github.com/fluture-js/Fluture#fork}
 */
export const fork = R.curry((leftFn, rightFn, future) =>
  Fluture.fork(leftFn)(rightFn)(future)
);

/**
 * @sig (a -> c) -> (b -> c) -> Future [a,b] -> Future [d,c]
 * @description {@link https://github.com/fluture-js/Fluture#coalesce}
 */
export const coalesce = R.curry((leftFn, rightFn, f) =>
  Fluture.coalesce(leftFn)(rightFn)(f)
);

/**
 * @sig Future a b -> Future [a,c] -> Future [a,Pair [b,c]]
 * @description {@link https://github.com/fluture-js/Fluture#both}
 */
export const both = R.curry((firstFuture, secondFuture) =>
  Fluture.both(firstFuture)(secondFuture)
);

/**
 * @sig number -> Array (Future [a,b]) -> Future [a,Array b]
 * @description {@link https://github.com/fluture-js/Fluture#parallel}
 */
export const parallel = R.curry((concurrency, futures) =>
  Fluture.parallel(concurrency)(futures)
);

/**
 * @sig number -> Object [string,Future [a,b]] -> Future [a,Object [string, b]]
 */
export const parallelObject = R.curry((concurrency, futures) =>
  Fluture.map(values => R.zipObj(R.keys(futures), values))(
    Fluture.parallel(concurrency)(R.values(futures))
  )
);

/**
 * @sig number -> b -> Future [a,b]
 * @description {@link https://github.com/fluture-js/Fluture#after}
 */
export const after = R.curry((delay, value) => Fluture.after(delay)(value));

/**
 * @sig (b -> c) -> Future [a,b] -> c
 * @description {@link https://github.com/fluture-js/Fluture#value}
 */
export const value = R.curry((fn, f) => Fluture.value(fn)(f));

/**
 * @deprecated
 * @sig number -> Future [a,b] -> Future [a,b]
 */
export const delay = R.curry((amount, future) =>
  R.compose(R.map(R.last), both(after(amount, true)))(future)
);

/**
 * @sig (b -> boolean) -> c -> Future [a,b] -> Future [c,b]
 */
export const filter = R.curry((predicate, rejectValue, f) =>
  R.chain(
    value =>
      predicate(value) ? Fluture.resolve(value) : Fluture.reject(rejectValue),
    f
  )
);
