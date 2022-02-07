import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Validation from '@Utility/validation';
import * as parsers from '@Utility/parsers';

const commonSchema = {
  henkilotunnus: [
    Validation.isSome,
    Validation.liftValidator(Validation.henkilotunnusValidator)
  ],
  etunimi: Validation.RequiredString(2, 200),
  sukunimi: Validation.RequiredString(2, 200),
  email: [...Validation.RequiredString(2, 200), Validation.emailValidator],
  puhelin: Validation.RequiredString(2, 200),

  'vastaanottajan-tarkenne': R.map(
    Validation.liftValidator,
    Validation.LimitedString(2, 200)
  ),
  jakeluosoite: Validation.RequiredString(2, 200),
  postinumero: [Validation.isRequired],
  postitoimipaikka: Validation.RequiredString(2, 200),

  wwwosoite: R.map(Validation.liftValidator, [Validation.urlValidator]),
  'api-key': [Validation.liftValidator(Validation.apiPasswordValidator)]
};

export const parseWWWOsoite = R.compose(
  Maybe.fromEmpty,
  parsers.addDefaultProtocol,
  R.trim
);

export const schema = maa =>
  Maybe.exists(R.equals('FI'), Either.toMaybe(maa))
    ? R.over(
        R.lensProp('postinumero'),
        R.append(Validation.postinumeroFIValidator),
        commonSchema
      )
    : R.over(
        R.lensProp('postinumero'),
        R.concat(R.__, Validation.LimitedString(2, 20)),
        commonSchema
      );
