import * as R from 'ramda';

export const isValid = R.prop('valid');
export const filterValid = R.filter(isValid);
