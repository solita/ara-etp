/**
 * @module Strings
 */
import * as R from 'ramda';

/**
 * @deprecated
 */
export const lpad = R.curry((targetLength, padString, txt) =>
  txt.padStart(targetLength, padString)
);
