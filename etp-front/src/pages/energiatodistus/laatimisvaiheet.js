import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

const vaiheet = [
  'rakennuslupa',
  'kayttoonotto',
  'olemassaolevarakennus',
  'rakennuslupaPerusparannus',
  'kayttoonottoPerusparannus'
];

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

export const isUudisrakennus = R.anyPass([
  isInVaihe(vaihe.rakennuslupa),
  isInVaihe(vaihe.kayttoonotto)
]);

export const isRakennuslupaPerusparannus = isInVaihe(
  vaihe.rakennuslupaPerusparannus
);
export const isKayttoonottoPerusparannus = isInVaihe(
  vaihe.kayttoonottoPerusparannus
);

/* Applicable for 2018 + 2013 versions */
export const isOlemassaOlevaRakennus = R.ifElse(
  R.propEq(2013, 'versio'),
  R.complement(R.path(['perustiedot', 'uudisrakennus'])),
  isInVaihe(vaihe.olemassaolevarakennus)
);

/**
 * Havainnointikaynti is required in 2026 for:
 * - olemassa oleva rakennus
 * - rakennuslupavaihe, laajamittainen perusparannus
 * - käyttöönottovaihe, laajamittainen perusparannus
 */
export const isHavainnointikayntiRequired = R.anyPass([
  isInVaihe(vaihe.olemassaolevarakennus),
  isInVaihe(vaihe.rakennuslupaPerusparannus),
  isInVaihe(vaihe.kayttoonottoPerusparannus),
]);
