import * as R from 'ramda';

export const linksForLaatija = laatija => [
  {
    text: 'navigation.energiatodistukset',
    href: '/energiatodistus/all'
  },
  { text: 'navigation.viestit', href: '/viestit' },
  {
    text: 'navigation.yritykset',
    href: `/laatija/${laatija.laatija}/yritykset`
  },
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
  { text: 'navigation.laatijat', href: '/laatija/all' },
  { text: 'navigation.omattiedot', href: '/myinfo' }
];

export const linksForLaskuttaja = _ => [
  {
    text: 'navigation.energiatodistukset',
    href: '/energiatodistus'
  },
  { text: 'navigation.laatijat', href: '/laatija/all' },
  { text: 'navigation.yritykset', href: '/yritys/all' },
  { text: 'navigation.laskutusajot', href: '/laskutus' }
];

export const linksForPaakayttaja = _ => [
  { text: 'navigation.tyojono', href: '/tyojono' },
  { text: 'navigation.kaytonvalvonta', href: '/kaytonvalvonta' },
  { text: 'navigation.halytykset', href: '/halytykset' },
  { text: 'navigation.kayttajat', href: '/kayttaja/all' },
  { text: 'navigation.yritykset', href: '/yritys/all' },
  { text: 'navigation.viestit', href: '/viestit' },
  { text: 'navigation.omattiedot', href: '/myinfo' }

];

export const linksForEnergiatodistus = R.curry((i18n, version, id) => [
  {
    text: `${i18n('navigation.et')} ${id}`,
    href: `/energiatodistus/${version}/${id}`,
    dynamic: true
  },
  {
    text: 'navigation.viestit',
    href: `/energiatodistus/${version}/${id}/viestit`
  },
  {
    text: 'navigation.liitteet',
    href: `/energiatodistus/${version}/${id}/liitteet`
  },
  {
    text: 'navigation.muutoshistoria',
    href: `/energiatodistus/${version}/${id}/muutoshistoria`
  }
]);

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
