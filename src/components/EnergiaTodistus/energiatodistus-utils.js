import * as R from "ramda";

import * as validation from "@Utility/validation";
import * as objects from '@Utility/objects';
import * as Maybe from "@Utility/maybe-utils";
import * as Either from '@Utility/either-utils';

export const emptyEnergiatodistus2018 = _ => ({
  perustiedot: {
    nimi: Maybe.None()
  }
});

export const emptyEnergiatodistus2013 = _ => ({
  perustiedot: {
    nimi: Maybe.None()
  }
});

export const parsers = {
  optionalText: R.compose(Maybe.fromEmpty, R.trim)
};

export const schema2018 = {
  perustiedot: {
    nimi: [
      validation.liftValidator(validation.minLengthConstraint(2)),
      validation.liftValidator(validation.maxLengthConstraint(200))
    ]
  }
};

export const schema2013 = {
  perustiedot: {
    nimi: [
      validation.liftValidator(validation.minLengthConstraint(2)),
      validation.liftValidator(validation.maxLengthConstraint(200))
    ]
  }
};

export const formatters = {
  optionalText: Maybe.orSome('')
};

export const isValidForm = R.compose(
  R.all(Either.isRight),
  R.filter(Either.isEither),
  objects.recursiveValues(Either.isEither),
  validation.validateModelObject
);

export const breadcrumb1stLevel = i18n => ({
  label: i18n('energiatodistus.breadcrumb.energiatodistus'),
  url: '/#/energiatodistukset'
});