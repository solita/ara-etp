/**
 * @module ToimintaAlueet
 * @description Utilities for toiminta-alueet
 */
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

/**
 * @typedef {Object} ToimintaAlue
 * @property {number} id
 * @property {string} label-fi
 * @property {string} label-sv
 * @property {boolean} valid
 */

/**
 * @sig ToimintaAlue -> Array ToimintaAlue -> Array ToimintaAlue
 */
export const toimintaalueetWithoutMain = R.curry(
  (mainToimintaalue, toimintaalueet) =>
    R.compose(
      Maybe.orSome(toimintaalueet),
      R.map(R.compose(R.applyTo(toimintaalueet), R.reject, R.equals))
    )(mainToimintaalue)
);

/**
 * @sig Maybe ToimintaAlue -> ToimintaAlue -> boolean
 */
export const isMainToimintaAlue = R.curry((mainToimintaalue, toimintaalue) =>
  R.compose(Maybe.isSome, R.filter(R.equals(toimintaalue)))(mainToimintaalue)
);
