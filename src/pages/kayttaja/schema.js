import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

const schema = {
  henkilotunnus: [
    Validation.isSome,
    Validation.liftValidator(Validation.henkilotunnusValidator)
  ],
  etunimi: Validation.RequiredString(2, 200),
  sukunimi: Validation.RequiredString(2, 200),
  email: [...Validation.RequiredString(2, 200), Validation.emailValidator],
  puhelin: Validation.RequiredString(2, 200),
  virtu: {
    localid: Validation.RequiredString(2, 200),
    organisaatio: Validation.RequiredString(2, 200)
  }
};

export const paakayttaja = R.dissoc('henkilotunnus', schema);
export const patevyydentoteaja = R.dissoc('virtu', schema);
export const laskuttaja = R.omit(['henkilotunnus', 'virtu'], schema);

export const formParsers = () => ({
  henkilotunnus: R.compose(Maybe.fromEmpty, R.trim),
  etunimi: R.trim,
  sukunimi: R.trim,
  email: R.trim,
  puhelin: R.trim,
  wwwosoite: R.compose(Maybe.fromEmpty, R.trim),
  virtu: { localid: R.trim, organisaatio: R.trim }
});
