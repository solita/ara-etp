import * as R from 'ramda';

export const linksForLaatija = laatija => [
  { text: 'Energiatodistukset', href: '/#/energiatodistus' },
  { text: 'Viestit', href: '/#/viestit' },
  { text: 'Yritykset', href: '/#/yritys' },
  { text: 'Omat tiedot', href: `/#/myinfo` }
];

export const linksForPatevyydentoteaja = _ => [
  { text: 'Laatijoiden Tuonti', href: '/#/laatija/upload' },
  { text: 'Laatijat', href: '/#/laatijat' },
  { text: 'Omat tiedot', href: `/#/myinfo` }
];

export const linksForLaskuttaja = _ => [
  { text: 'Energiatodistukset', href: '/#/energiatodistus' },
  { text: 'Laatijat', href: '/#/laatijat' },
  { text: 'Yritykset', href: '/#/yritys' },
  { text: 'Laskutusajot', href: `/#/laskutus` }
];

export const linksForPaakayttaja = _ => [
  { text: 'Työjono', href: '/#/tyojono' },
  { text: 'Käytönvalvonta', href: '/#/viestit' },
  { text: 'Hälytykset', href: '/#/halytys' },
  { text: 'Käyttäjät', href: `/#/kayttaja` },
  { text: 'Yritykset', href: `/#/yritys` },
  { text: 'Viestit', href: `/#/viestit` }
];

const kayttajaLinksMap = Object.freeze({
  0: linksForLaatija,
  1: linksForPatevyydentoteaja,
  2: linksForPaakayttaja
});

export const linksForKayttaja = kayttaja =>
  R.compose(
    R.applyTo(kayttaja),
    R.prop(R.__, kayttajaLinksMap),
    R.prop('rooli')
  )(kayttaja);
