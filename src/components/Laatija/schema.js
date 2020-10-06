import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

export const schema = {
  henkilotunnus: [
    Validation.isSome,
    Validation.liftValidator(Validation.henkilotunnusValidator)],
  etunimi: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  sukunimi: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  email: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200),
    Validation.emailValidator
  ],
  puhelin: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  'vastaanottajan-tarkenne': R.map(Validation.liftValidator, [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ]),
  jakeluosoite: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  postinumero: [Validation.isRequired, Validation.postinumeroValidator],
  postitoimipaikka: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  wwwosoite: R.map(Validation.liftValidator, [Validation.urlValidator]),

  'api-key': R.map(Validation.liftValidator,[
    Validation.minLengthConstraint(8),
    Validation.maxLengthConstraint(30)
  ])
};

export const formParsers = () => ({
  'vastaanottajan-tarkenne': R.compose(Maybe.fromEmpty, R.trim),
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim,
  wwwosoite: R.compose(Maybe.fromEmpty, R.trim)
});
