import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Kayttajat from '@Utility/kayttajat';
import * as Maybe from '@Utility/maybe-utils';

export const fromLaatijaForm = R.evolve({ toteamispaivamaara: Either.right });

export const toLaatijaForm = R.evolve({ toteamispaivamaara: Either.Right });

export const emptyLaatija = {
  login: Maybe.None(),
  passivoitu: false,
  rooli: Maybe.Some(Kayttajat.role.laatija),
  etunimi: '',
  sukunimi: '',
  email: '',
  puhelin: '',
  partner: true,
  henkilotunnus: Maybe.None(),
  toimintaalue: Maybe.None(),
  muuttoimintaalueet: [],
  wwwosoite: Maybe.None(),
  maa: Either.Right('FI'),
  patevyystaso: 1,
  verifytime: Maybe.None(),
  'vastaanottajan-tarkenne': Maybe.None(),
  jakeluosoite: '',
  postinumero: '',
  postitoimipaikka: '',
  laskutuskieli: 0,
  toteamispaivamaara: Either.Right(new Date()),
  'voimassaolo-paattymisaika': new Date(),
  'api-key': Maybe.None(),
  julkinenwwwosoite: false,
  julkinenemail: false,
  julkinenosoite: false,
  julkinenpuhelin: false,
  julkinenpostinumero: false
};
