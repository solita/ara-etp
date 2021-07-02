import * as validation from '@Utility/validation';
import * as R from 'ramda';

export const ketju = {
  subject: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ],
  body: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(4000)
  ]
};
