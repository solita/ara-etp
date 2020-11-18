import * as R from 'ramda';

export const laatijaRole = 0;
export const patevyydentoteajaRole = 1;
export const paakayttajaRole = 2;

/** Deprecated - use functions from utils/kayttajat instead */
export const kayttajaHasAccessToResource = R.curry((roolit, kayttaja) =>
  R.compose(R.applyTo(roolit), R.includes, R.prop('rooli'))(kayttaja)
);
