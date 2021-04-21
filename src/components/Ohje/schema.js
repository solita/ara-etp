import * as validation from '@Utility/validation';

export const sivu = {
  subject: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ],
  body: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(10000)
  ]
};
};
