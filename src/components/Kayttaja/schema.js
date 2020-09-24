import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

const formSchema = {
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
  virtu: {localid: [Validation.isSome],
          organisaatio: [Validation.isSome]}
};

export const paakayttaja = R.dissoc('henkilotunnus', formSchema);
export const patevyydentoteaja = R.dissoc('virtu', formSchema);

export const formParsers = () => ({
  henkilotunnus: R.compose(Maybe.fromEmpty, R.trim),
  etunimi: R.trim,
  sukunimi: R.trim,
  email: R.trim,
  puhelin: R.trim,
  wwwosoite: R.compose(Maybe.fromEmpty, R.trim),
  virtu: {localid: R.compose(Maybe.fromEmpty, R.trim),
          organisaatio: R.compose(Maybe.fromEmpty, R.trim)}
});
