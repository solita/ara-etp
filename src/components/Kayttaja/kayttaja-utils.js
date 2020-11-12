import * as R from 'ramda';

export const laatijaRole = 0;
export const patevyydentoteajaRole = 1;
export const paakayttajaRole = 2;

export const kayttajaHasAccessToResource = R.curry((roolit, kayttaja) =>
  R.compose(R.applyTo(roolit), R.includes, R.prop('rooli'))(kayttaja)
);

export const isPaakayttaja = kayttajaHasAccessToResource([paakayttajaRole]);
export const isPatevyydentoteaja = kayttajaHasAccessToResource([
  patevyydentoteajaRole
]);
