import * as Validation from '@Utility/validation';

export const schema = {
  nimi: [Validation.isRequired, Validation.maxLengthConstraint(300)],
  url: [Validation.isRequired, Validation.urlValidator]
};

export const empty = _ => ({ nimi: '', url: '' });
