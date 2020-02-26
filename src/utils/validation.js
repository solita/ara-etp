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
    R.converge(R.equals, [ytunnusChecksum, R.compose(parseInt, R.nth(8))])]);