import NavigationTabBar from './navigation-tab-bar';
import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
import * as R from 'ramda';

export default { title: 'NavigationTabBar' };

export const withMultipleLinks = () => ({
  Component: NavigationTabBar,
  props: {
    idTranslate: {
      yritys: {},
      kayttaja: {},
      energiatodistus: {},
      viesti: {}
    },
    location: '/energiatodistus/all',
    whoami: KayttajaApi.deserialize({
      email: 'paakayttaja@solita.fi',
      partner: false,
      'titteli-sv': null,
      verifytime: null,
      puhelin: '0501234567',
      sukunimi: 'Pääkäyttäjä',
      virtu: {
        localid: 'vvirkamies',
        organisaatio: 'testivirasto.fi'
      },
      rooli: 2,
      id: 18,
      cognitoid: 'paakayttaja@solita.fi',
      henkilotunnus: null,
      etunimi: 'Päivi',
      organisaatio: '',
      'titteli-fi': null
    }),
    i18n: R.identity,
    config: {
      isDev: true
    }
  }
});
