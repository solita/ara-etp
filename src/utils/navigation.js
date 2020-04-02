import * as R from 'ramda';

export const linksForLaatija = laatija => [
  {
    text: 'navigation.energiatodistukset',
    href: '/energiatodistus'
  },
  { text: 'navigation.viestit', href: '/viestit' },
  { text: 'navigation.yritykset', href: '/yritys' },
  {
    text: 'navigation.omattiedot',
    href: '/myinfo',
    activePath: `/laatija/${laatija.id}`
  }
];

export const linksForPatevyydentoteaja = _ => [
  {
    text: 'navigation.laatijoidentuonti',
    href: '/laatija/upload'
  },
  { text: 'navigation.laatijat', href: '/laatijat' },
  { text: 'navigation.omattiedot', href: '/myinfo' }
];

export const linksForLaskuttaja = _ => [
  {
    text: 'navigation.energiatodistukset',
    href: '/energiatodistus'
  },
  { text: 'navigation.laatijat', href: '/laatijat' },
  { text: 'navigation.yritykset', href: '/yritys' },
  { text: 'navigation.laskutusajot', href: '/laskutus' }
];

export const linksForPaakayttaja = _ => [
  { text: 'navigation.tyojono', href: '/tyojono' },
  { text: 'navigation.kaytonvalvonta', href: '/kaytonvalvonta' },
  { text: 'navigation.halytykset', href: '/halytys' },
  { text: 'navigation.kayttajat', href: '/kayttaja' },
  { text: 'navigation.yritykset', href: '/yritys' },
  { text: 'navigation.viestit', href: '/viestit' }
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
