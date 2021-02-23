import * as R from 'ramda';

const roles = ['laatija', 'patevyydentoteaja', 'paakayttaja', 'laskuttaja'];
export const role = R.compose(R.map(parseInt), R.invertObj)(roles);
export const roleKey = id => roles[id];

/** predicate functions for role id */
export const isLaatijaRole = R.equals(role.laatija);
export const isPatevyydentoteajaRole = R.equals(role.patevyydentoteaja);
export const isPaakayttajaRole = R.equals(role.paakayttaja);

/** predicate functions for whoami object */
export const isLaatija = R.propSatisfies(isLaatijaRole, 'rooli');
export const isPatevyydentoteaja = R.propSatisfies(
  isPatevyydentoteajaRole,
  'rooli'
);
export const isPaakayttaja = R.propSatisfies(isPaakayttajaRole, 'rooli');

export const fullName = kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`;
