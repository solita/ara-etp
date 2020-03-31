import * as R from 'ramda';

export const linksForKayttaja = kayttaja => [
  { text: 'Energiatodistukset', href: '/#/energiatodistus' },
  { text: 'Viestit', href: '/#/viestit' },
  { text: 'Yritykset', href: '/#/yritys' },
  { text: 'Omat tiedot', href: `/#/laatija/${kayttaja.id}` }
];
