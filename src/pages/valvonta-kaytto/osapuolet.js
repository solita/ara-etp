import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const isOmistaja = R.propEq('rooli-id', Maybe.Some(0));
export const otherRooli = R.propEq('rooli-id', Maybe.Some(2));

export const toimitustapa = {
  suomifi: R.propEq('toimitustapa-id', Maybe.Some(0)),
  email: R.propEq('toimitustapa-id', Maybe.Some(1)),
  other: R.propEq('toimitustapa-id', Maybe.Some(2))
};

const emptyOsapuoli = {
  'rooli-id': Maybe.None(),
  'rooli-description': Maybe.None(),
  email: Maybe.None(),
  puhelin: Maybe.None(),
  'vastaanottajan-tarkenne': Maybe.None(),
  jakeluosoite: Maybe.None(),
  postinumero: Maybe.None(),
  postitoimipaikka: Maybe.None(),
  maa: Maybe.None(),
  'toimitustapa-id': Maybe.None(),
  'toimitustapa-description': Maybe.None()
};

export const emptyHenkilo = _ =>
  R.mergeLeft(emptyOsapuoli, {
    etunimi: '',
    sukunimi: '',
    henkilotunnus: Maybe.None()
  });

export const emptyYritys = _ =>
  R.mergeLeft(emptyOsapuoli, {
    nimi: '',
    ytunnus: Maybe.None()
  });

const invalidOsoite = R.anyPass([
  R.propSatisfies(Maybe.isNone, 'jakeluosoite'),
  R.propSatisfies(Maybe.isNone, 'maa')
]);

const invalidEmail = R.allPass([
  toimitustapa.email,
  R.propSatisfies(Maybe.isNone, 'email')
]);

export const toimitustapaErrorKey = {
  yritys: yritys =>
    toimitustapa.suomifi(yritys) &&
    (Maybe.isNone(yritys.ytunnus) || invalidOsoite(yritys))
      ? Maybe.Some('suomifi-yritys')
      : invalidEmail(yritys)
      ? Maybe.Some('email')
      : Maybe.None(),
  henkilo: henkilo =>
    toimitustapa.suomifi(henkilo) &&
    (Maybe.isNone(henkilo.henkilotunnus) || invalidOsoite(henkilo))
      ? Maybe.Some('suomifi-henkilo')
      : invalidEmail(henkilo)
      ? Maybe.Some('email')
      : Maybe.None()
};
