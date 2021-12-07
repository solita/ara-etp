import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Kayttajat from '@Utility/kayttajat';
import * as Validation from '@Utility/validation';
import * as Tila from '@Pages/yritys/laatija-yritys-tila';

export const emptyYritys = {
  deleted: false,
  ytunnus: '',
  nimi: '',
  'vastaanottajan-tarkenne': Maybe.None(),
  jakeluosoite: '',
  postinumero: '',
  postitoimipaikka: '',
  maa: Either.Right('FI'),
  laskutuskieli: 0,
  verkkolaskuosoite: Maybe.None(),
  verkkolaskuoperaattori: Either.Right(Maybe.None())
};

const commonSchema = {
  ytunnus: [Validation.isRequired, Validation.ytunnusValidator],
  nimi: Validation.RequiredString(2, 200),
  'vastaanottajan-tarkenne': R.map(
    Validation.liftValidator,
    Validation.LimitedString(2, 200)
  ),
  jakeluosoite: Validation.RequiredString(2, 200),
  postinumero: [Validation.isRequired],
  postitoimipaikka: Validation.RequiredString(2, 200),
  maa: [],
  verkkolaskuosoite: R.map(Validation.liftValidator, [
    Validation.VerkkolaskuosoiteValidator
  ]),
  verkkolaskuoperaattori: []
};

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

export const parseVerkkolaskuosoite = R.compose(
  Maybe.fromEmpty,
  R.trim,
  R.toUpper,
  R.replace(/\s/g, '')
);

const isInYritys = (laatijat, whoami) =>
  R.any(R.propEq('id', whoami.id), R.filter(Tila.isAccepted, laatijat));

export const hasModifyPermission = R.curry(
  (laatijat, whoami) =>
    Kayttajat.isPaakayttaja(whoami) || isInYritys(laatijat, whoami)
);
