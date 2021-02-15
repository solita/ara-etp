import * as R from 'ramda';

export const lpad = R.curry((targetLength, padString, txt) =>
  txt.padStart(targetLength, padString)
);
