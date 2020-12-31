import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';
import * as parsers from '@Utility/parsers';

const RequiredString = (min, max) => ([
  Validation.isRequired,
  ...Validation.LimitedString(min, max)
]);

export const schema = {
  henkilotunnus: [
    Validation.isSome,
    Validation.liftValidator(Validation.henkilotunnusValidator)],
  etunimi: RequiredString(2, 200),
  sukunimi: RequiredString(2, 200),
  email: [
    ...RequiredString(2, 200),
    Validation.emailValidator
  ],
  puhelin: RequiredString(2, 200),

  'vastaanottajan-tarkenne':
    R.map(Validation.liftValidator, Validation.LimitedString(2,200)),
  jakeluosoite: RequiredString(2, 200),
  postinumero: [Validation.isRequired, Validation.postinumeroValidator],
  postitoimipaikka: RequiredString(2, 200),

  wwwosoite: R.map(Validation.liftValidator, [Validation.urlValidator]),
  'api-key': R.map(Validation.liftValidator, Validation.LimitedString(8, 200))
};

export const formParsers = () => ({
  'vastaanottajan-tarkenne': R.compose(Maybe.fromEmpty, R.trim),
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim,
  wwwosoite: R.compose(Maybe.fromEmpty, parsers.addDefaultProtocol, R.trim)
});
