import * as R from 'ramda';

const roles = ['laatija', 'patevyydentoteaja', 'paakayttaja'];
export const role = R.compose(R.map(parseInt), R.invertObj)(roles);
export const roleKey = id => roles[id];

export const isLaatija = R.equals(role.laatija);
export const isPatevyydentoteaja = R.equals(role.patevyydentoteaja);
export const isPaakayttaja = R.equals(role.paakayttaja);
