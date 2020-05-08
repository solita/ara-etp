import * as R from 'ramda';

export const role = {
  laatija: 0,
  patevyydentoteaja: 1,
  paakayttaja: 2
}

export const isLaatija = R.equals(role.laatija);
export const isPatevyydentoteaja = R.equals(role.patevyydentoteaja);
export const isPaakayttaja = R.equals(role.paakayttaja);