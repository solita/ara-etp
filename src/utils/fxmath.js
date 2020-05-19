import * as R from 'ramda';

const shift = (number, shifting) => {
  const [significand, exponent] = number.toExponential().split("e");
  return parseFloat(significand + "e" + (parseInt(exponent) + shifting));
};

/**
 * Rounding based on
 * https://wiki.developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/round$revision/1383484
 */
export const round = R.curry((precision, number) =>
  shift(Math.round(shift(number, +precision)), -precision));
