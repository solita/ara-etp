import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const isOmistaja = R.propEq(Maybe.Some(0), 'rooli-id');
export const otherRooli = R.propEq(Maybe.Some(2), 'rooli-id');

export const toimitustapa = {
  suomifi: R.propEq(Maybe.Some(0), 'toimitustapa-id'),
  email: R.propEq(Maybe.Some(1), 'toimitustapa-id'),
  other: R.propEq(Maybe.Some(2), 'toimitustapa-id')
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

const yritysErrorKey = yritys =>
  toimitustapa.suomifi(yritys) &&
  (Maybe.isNone(yritys.ytunnus) || invalidOsoite(yritys))
    ? Maybe.Some('suomifi-yritys')
    : invalidEmail(yritys)
      ? Maybe.Some('email')
      : Maybe.None();

const henkiloErrorKey_ = henkilo =>
  toimitustapa.suomifi(henkilo) &&
  (Maybe.isNone(henkilo.henkilotunnus) || invalidOsoite(henkilo))
    ? Maybe.Some('suomifi-henkilo')
    : invalidEmail(henkilo)
      ? Maybe.Some('email')
      : Maybe.None();

const henkiloErrorKey = henkilo => {
  if (toimitustapa.suomifi(henkilo) && !isOmistaja(henkilo)) {
    return Maybe.Some('suomifi-henkilo-omistaja-required');
  } else if (
    toimitustapa.suomifi(henkilo) &&
    (Maybe.isNone(henkilo.henkilotunnus) || invalidOsoite(henkilo))
  ) {
    return Maybe.Some('suomifi-henkilo');
  } else if (invalidEmail(henkilo)) {
    return Maybe.Some('email');
  } else {
    return Maybe.None();
  }
};

export const toimitustapaErrorKey = {
  yritys: yritysErrorKey,
  henkilo: henkiloErrorKey
};

/**
 * Osapuoli is henkilo if the object has props etunimi and sukunimi, otherwise it's yritys
 * @param {Object} osapuoli
 * @returns {('henkilo'|'yritys')}
 */
export const getOsapuoliType = osapuoli =>
  R.has('etunimi', osapuoli) && R.has('sukunimi', osapuoli)
    ? 'henkilo'
    : 'yritys';
