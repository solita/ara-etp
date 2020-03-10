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

export const lengthConstraint = (min, max) => ({
  predicate: R.compose(R.allPass([R.lte(min), R.gte(max)]), R.length),
  label: R.pipe(
    R.applyTo('validation.length-constraint'),
    R.replace('{min}', min),
    R.replace('{max}', max)
  )
});

export const isUrl = R.test(
  /(http(s)?:\/\/.)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)/
);

export const isPostinumero = R.test(/^\d{5}$/);
