import * as R from 'ramda';

import * as Validation from '@Utility/validation';

import * as Toimenpiteet from './toimenpiteet';

const OptionalLimitedString = (min, max) => R.map(
  Validation.liftValidator,
  Validation.LimitedString(2, 200))

export const kohde = {
  rakennustunnus: [
    Validation.liftValidator(Validation.rakennustunnusValidator)
  ],
  katuosoite: Validation.RequiredString(2, 200),
  postinumero: [
    Validation.liftValidator(Validation.postinumeroValidator)
  ],
  ilmoituspaikka_description: OptionalLimitedString(2, 200),
  ilmoitustunnus: OptionalLimitedString(2, 200)
};

const osapuoli = {
  email: OptionalLimitedString(2, 200),
  puhelin: OptionalLimitedString(2, 200),
  'vastanottajan-tarkenne': OptionalLimitedString(2, 200),
  postinumero: OptionalLimitedString(2, 200),
  postitoimipaikka: OptionalLimitedString(2, 200),
  rooli_description: OptionalLimitedString(2, 200),
  toimitustapa_description: OptionalLimitedString(2, 200),
};

export const henkilo = R.mergeLeft(osapuoli, {
  henkilotunnus: [
    Validation.liftValidator(Validation.henkilotunnusValidator)
  ],
  etunimi: Validation.RequiredString(2, 200),
  sukunimi: Validation.RequiredString(2, 200),
});

export const yritys = R.mergeLeft(osapuoli, {
  ytunnus: [
    Validation.liftValidator(Validation.ytunnusValidator)
  ],
  nimi: Validation.RequiredString(2, 200)
});

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
