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

const roles = ['laatija', 'patevyydentoteaja', 'paakayttaja', 'laskuttaja'];

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
 * @sig Kayttaja -> boolean
 */
export const isLaatija = R.propSatisfies(isLaatijaRole, 'rooli');
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
export const isPaakayttajaOrLaskuttaja = R.anyPass([
  isPaakayttaja,
  isLaskuttaja
]);

/**
 * @sig Kayttaja -> string
 */
export const fullName = kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`;
