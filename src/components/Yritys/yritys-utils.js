import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Future from '@Utility/future-utils';
import * as validation from '@Utility/validation';

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
  verkkolaskuoperaattori: Maybe.None()
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
  verkkolaskuosoite: R.compose(Maybe.fromEmpty, R.trim),
  verkkolaskuoperaattori: R.compose(Maybe.fromEmpty, R.trim)
});
