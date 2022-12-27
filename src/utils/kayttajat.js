/**
 * @module Kayttajat
 * @description Utilities for handling kayttajat
 */

/**
 * @typedef {number} Rooli
 */

/**
 * @typedef {Object} Virtu
 * @property {string} localid
 * @property {string} organisaatio
 */

/**
 * @typedef {Object} Kayttaja
 * @property {number} id
 * @property {string} etunimi
 * @property {string} sukunimi
 * @property {string} email
 * @property {Rooli} rooli
 * @property {string} cognitoid
 * @property {string} henkilotunnus
 * @property {Virtu?} virtu
 */

import * as R from 'ramda';
import * as dfns from 'date-fns';
import * as Maybe from '@Utility/maybe-utils';

const roles = [
  'laatija',
  'patevyydentoteaja',
  'paakayttaja',
  'laskuttaja',
  'aineistoasiakas'
];

/**
 * @enum {Rooli}
 */
export const role = R.compose(R.map(parseInt), R.invertObj)(roles);

/**
 * @member
 * @sig string -> Rooli
 */
export const roleKey = id => roles[id];

/**
 * @sig Rooli -> boolean
 */
export const isLaatijaRole = R.equals(role.laatija);
/**
 * @sig Rooli -> boolean
 */
export const isPatevyydentoteajaRole = R.equals(role.patevyydentoteaja);
/**
 * @sig Rooli -> boolean
 */
export const isPaakayttajaRole = R.equals(role.paakayttaja);
/**
 * @sig Rooli -> boolean
 */
export const isLaskuttajaRole = R.equals(role.laskuttaja);
/**
 * @sig Rooli -> boolean
 */
const isAineistoasiakasRole = R.equals(role.aineistoasiakas);
/**
 * @sig Rooli -> boolean
 */
export const isSystemRole = R.equals(-1);

/**
 * User is any laatija.
 * @sig Kayttaja -> boolean
 */
export const isLaatija = R.propSatisfies(isLaatijaRole, 'rooli');

/**
 * User is an accredited laatija.
 * @sig Kayttaja -> boolean
 */
export const isAccreditedLaatija = R.allPass([
  isLaatija,
  R.complement(R.prop('partner'))
]);
/**
 * @sig Kayttaja -> boolean
 */
export const isPatevyydentoteaja = R.propSatisfies(
  isPatevyydentoteajaRole,
  'rooli'
);
export const isPaakayttaja = R.propSatisfies(isPaakayttajaRole, 'rooli');
/**
 * @sig Kayttaja -> boolean
 */
export const isLaskuttaja = R.propSatisfies(isLaskuttajaRole, 'rooli');
/**
 * @sig Kayttaja -> boolean
 */
export const isAineistoasiakas = R.propSatisfies(
  isAineistoasiakasRole,
  'rooli'
);
/**
 * @sig Kayttaja -> boolean
 */
export const isPaakayttajaOrLaskuttaja = R.anyPass([
  isPaakayttaja,
  isLaskuttaja
]);

/**
 * @sig Kayttaja -> boolean
 */
export const isSystem = R.propSatisfies(isSystemRole, 'rooli');

/**
 * @sig Kayttaja -> string
 */
export const fullName = kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`;

export const isSelf = R.curry((whoami, id) => whoami.id === id);

export const format = R.curry((selfLabel, kayttajat, whoami, id) =>
  R.ifElse(
    isSelf(whoami),
    R.always(selfLabel),
    R.compose(Maybe.fold('', fullName), id =>
      Maybe.find(R.propEq('id', id), kayttajat)
    )
  )(id)
);

export const isVerified = kayttaja =>
  Maybe.fold(
    false,
    verifytime => dfns.isAfter(dfns.addMonths(verifytime, 6), new Date()),
    kayttaja.verifytime
  );

export const isVerificationActive = (whoami, kayttaja) =>
  !isVerified(kayttaja) && isSelf(whoami, kayttaja.id);
