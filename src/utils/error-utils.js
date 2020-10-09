import * as R from 'ramda';

export const httpError = R.curry((errorMessages, response) => ({
  response,
  message: R.propOr('default_error', response, errorMessages)
}));
