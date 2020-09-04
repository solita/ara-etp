import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

const vaiheet = [
  'rakennuslupa',
  'kayttoonotto',
  'olemassaolevarakennus'
];

const vaihe = R.compose(R.map(parseInt), R.invertObj)(vaiheet);

const isInVaihe = R.curry((vaiheId, energiatodistus) => R.compose(
  Maybe.isSome,
  R.filter(R.equals(vaiheId)),
  R.path(['perustiedot', 'laatimisvaihe'])
)(energiatodistus));

export const isRakennuslupa = isInVaihe(vaihe.rakennuslupa);
export const isKayttoonotto = isInVaihe(vaihe.kayttoonotto);
export const isOlemassaOlevaRakennus = isInVaihe(vaihe.olemassaolevarakennus);
