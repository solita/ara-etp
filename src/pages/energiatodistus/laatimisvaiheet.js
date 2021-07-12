import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

const vaiheet = ['rakennuslupa', 'kayttoonotto', 'olemassaolevarakennus'];

const vaihe = R.compose(R.map(parseInt), R.invertObj)(vaiheet);

const isInVaihe = R.curry((vaiheId, energiatodistus) =>
  R.compose(
    Maybe.isSome,
    R.filter(R.equals(vaiheId)),
    R.path(['perustiedot', 'laatimisvaihe'])
  )(energiatodistus)
);

/*
Applicable only for 2018 version
This information cannot be inferred from 2013 et
*/
export const isRakennuslupa = isInVaihe(vaihe.rakennuslupa);
export const isKayttoonotto = isInVaihe(vaihe.kayttoonotto);

/* Applicable for 2018 + 2013 versions */
export const isOlemassaOlevaRakennus = R.ifElse(
  R.propEq('versio', 2013),
  R.complement(R.path(['perustiedot', 'uudisrakennus'])),
  isInVaihe(vaihe.olemassaolevarakennus)
);
