import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Kayttajat from '@Utility/kayttajat';
import * as validation from '@Utility/validation';
import * as Tila from '@Component/Yritys/laatija-yritys-tila';

export const emptyYritys = () => ({
  ytunnus: '',
  nimi: '',
  'vastaanottajan-tarkenne': Maybe.None(),
  jakeluosoite: '',
  postinumero: '',
  postitoimipaikka: '',
  maa: '',
  laskutuskieli: 0,
  verkkolaskuosoite: Maybe.None(),
  verkkolaskuoperaattori: Either.Right(Maybe.None())
});

export const formSchema = () => ({
  ytunnus: [validation.isRequired, validation.ytunnusValidator],
  nimi: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ],
  'vastaanottajan-tarkenne': R.map(validation.liftValidator, [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ]),
  jakeluosoite: [validation.isRequired],
  postinumero: [validation.isRequired, validation.postinumeroValidator],
  postitoimipaikka: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ],
  maa: [],
  verkkolaskuosoite: R.map(validation.liftValidator, [
    validation.VerkkolaskuosoiteValidator
  ]),
  verkkolaskuoperaattori: []
});

export const formParsers = () => ({
  ytunnus: R.trim,
  nimi: R.trim,
  'vastaanottajan-tarkenne': R.compose(Maybe.fromEmpty, R.trim),
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim,
  maa: R.trim,
  verkkolaskuosoite: R.compose(
    Maybe.fromEmpty,
    R.trim,
    R.toUpper,
    R.replace(/\s/g, '')
  ),
  verkkolaskuoperaattori: R.compose(Maybe.fromEmpty, R.trim)
});

const isInYritys = (laatijat, whoami) =>
  R.any(R.propEq('id', whoami.id), R.filter(Tila.isAccepted, laatijat));

export const hasModifyPermission = R.curry(
  (laatijat, whoami) =>
    Kayttajat.isPaakayttaja(whoami) || isInYritys(laatijat, whoami)
);
