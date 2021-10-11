import * as Validation from '@Utility/validation';

export const Virhetype = {
  'label-fi': Validation.RequiredString(2, 200),
  'label-sv': Validation.RequiredString(2, 200),
  'description-fi': Validation.RequiredString(2, 4000),
  'description-sv': Validation.RequiredString(2, 4000)
};

export const newVirhetype = {
  ordinal: 1,
  'label-fi': '',
  'label-sv': '',
  'description-fi': '',
  'description-sv': '',
  valid: true
};
