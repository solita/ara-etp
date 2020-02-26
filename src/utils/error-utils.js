import * as R from 'ramda';

export const httpError = R.curry((errorMessages, statusCode) => ({
  statusCode,
  message: R.propOr('default_error', statusCode, errorMessages)
}));
