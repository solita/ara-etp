import * as R from 'ramda';

import * as Validation from '@Utility/validation';

import * as Toimenpiteet from './toimenpiteet';

export const kohde = {
  rakennustunnus: [
    Validation.liftValidator(Validation.rakennustunnusValidator)
  ],
  katuosoite: Validation.RequiredString(2, 200),
  postinumero: [
    Validation.liftValidator(Validation.postinumeroValidator)
  ],
  ilmoituspaikka_description: R.map(
    Validation.liftValidator,
    Validation.LimitedString(2, 200)),
  ilmoitustunnus: R.map(
    Validation.liftValidator,
    Validation.LimitedString(2, 200))
};

const description = R.map(
  Validation.liftValidator,
  Validation.LimitedString(2, 4000)
);

const addRequiredValidator = when =>
  when ? R.prepend(Validation.isSome) : R.identity;

export const toimenpideSave = {
  publish: false,
  'deadline-date': [],
  'template-id': [],
  description: description,
  'severity-id': []
};

export const toimenpidePublish = (templates, toimenpide) =>
  R.evolve(
    {
      publish: R.T,
      'deadline-date': addRequiredValidator(
        Toimenpiteet.hasDeadline(toimenpide)
      ),
      'template-id': addRequiredValidator(!R.isEmpty(templates))
    },
    toimenpideSave
  );

export const note = {
  description: Validation.LimitedString(2, 4000)
};
