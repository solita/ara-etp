/**
 * @module FxMath
 */
import * as R from 'ramda';

const shift = (number, shifting) => {
  const [significand, exponent] = number.toExponential().split('e');
  return parseFloat(significand + 'e' + (parseInt(exponent) + shifting));
};

/**
 * @sig number -> number -> number
 * @description Rounds a number <br>Rounding based on <br>{@link https://wiki.developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/round$revision/1383484}
 * @example round(2, 1.005) // 1.05
 *
 */
export const round = R.curry((precision, number) =>
  shift(Math.round(shift(number, +precision)), -precision)
);
