import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';

export const ytunnusChecksum = R.compose(
  R.unless(R.equals(0), R.subtract(11)),
  R.modulo(R.__, 11),
  R.reduce(R.add, 0),
  R.zipWith(R.multiply, [7, 9, 10, 5, 8, 4, 2]),
  R.map(parseInt),
  R.slice(0, 7)
);

export const isValidYtunnus = R.allPass([
  R.test(/^\d{7}-\d$/),
  R.converge(R.equals, [ytunnusChecksum, R.compose(parseInt, R.nth(8))])
]);

export const isFilled = R.complement(R.isEmpty);

export const isRequired = {
  predicate: R.complement(R.isEmpty),
  label: R.applyTo('validation.required')
};

export const interpolate = R.curry((template, values) =>
  R.reduce((result, value) => R.replace(value[0], value[1], result),
    template, R.toPairs(values)));

export const lengthConstraint = (predicate, name, labelValues) => ({
  predicate: R.compose(predicate, R.length),
  label: R.compose(
    interpolate(R.__, labelValues),
    R.applyTo('validation.' + name + '-length')
  )
});

export const minLengthConstraint = min =>
  lengthConstraint(R.lte(min), 'min', {'{min}': min});

export const maxLengthConstraint = max =>
  lengthConstraint(R.gte(max), 'max', {'{max}': max});

export const isUrl = R.test(
  /(http(s)?:\/\/.)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)/
);

export const urlValidator = {
  predicate: isUrl,
  label: R.applyTo('validation.invalid-url')
};

export const liftValidator = (validator) =>
  R.over(R.lensProp('predicate'),
    predicate => R.compose(Maybe.orSome(true), R.lift(predicate)), //value => value.map(predicate).orSome(true),
    validator);

export const isPostinumero = R.test(/^\d{5}$/);

export const validate = (validators, value) =>
  Maybe.fromUndefined(R.find(R.compose(
    R.not,
    R.applyTo(value),
    R.prop('predicate')), validators))
    .toEither(value).swap();
