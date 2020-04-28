import * as validation from '@Utility/validation';
import * as parsers from '@Utility/parsers';

const String = max => ({
  parse: parsers.optionalString,
  validators: [
    validation.liftValidator(validation.minLengthConstraint(2)),
    validation.liftValidator(validation.maxLengthConstraint(max))
  ]
});

export const schema = {
  perustiedot: {
    yritys: {
      nimi: String(200)
    },
  },
};
