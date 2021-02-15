import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

const RequiredString = (min, max) => [
  Validation.isRequired,
  Validation.minLengthConstraint(min),
  Validation.maxLengthConstraint(max)
];

const schema = {
  henkilotunnus: [
    Validation.isSome,
    Validation.liftValidator(Validation.henkilotunnusValidator)
  ],
  etunimi: RequiredString(2, 200),
  sukunimi: RequiredString(2, 200),
  email: [...RequiredString(2, 200), Validation.emailValidator],
  puhelin: RequiredString(2, 200),
  virtu: {
    localid: RequiredString(2, 200),
    organisaatio: RequiredString(2, 200)
  }
};

export const paakayttaja = R.dissoc('henkilotunnus', schema);
export const patevyydentoteaja = R.dissoc('virtu', schema);

export const formParsers = () => ({
  henkilotunnus: R.compose(Maybe.fromEmpty, R.trim),
  etunimi: R.trim,
  sukunimi: R.trim,
  email: R.trim,
  puhelin: R.trim,
  wwwosoite: R.compose(Maybe.fromEmpty, R.trim),
  virtu: { localid: R.trim, organisaatio: R.trim }
});
