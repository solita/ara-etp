import * as R from 'ramda';
import * as validation from '@Utility/validation';
import * as MD from '@Component/text-editor/markdown';

export const ketju = {
  subject: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ],
  body: R.map(MD.plainTextValidator, [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(4000)
  ])
};
