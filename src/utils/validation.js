import * as R from 'ramda';

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

export const lengthConstraint = (predicate, name, values) => ({
  predicate: R.compose(predicate, R.length),
  label: R.compose(
    interpolate(R.__, values),
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

export const isPostinumero = R.test(/^\d{5}$/);
