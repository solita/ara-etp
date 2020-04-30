import { wrap } from 'svelte-spa-router';

import Yritys from '@Component/Yritys/Yritys';
import Kayttaja from '@Component/Kayttaja/Kayttaja';
import Viestit from '@Component/Viestit/Viestit';
import Laatija from '@Component/Laatija/Laatija';
import Halytykset from '@Component/Halytykset/Halytykset';
import Kaytonvalvonta from '@Component/Kaytonvalvonta/Kaytonvalvonta';
import Tyojono from '@Component/Tyojono/Tyojono';
import Energiatodistus from '@Component/Energiatodistus/Energiatodistus';
import NotFound from '@Component/NotFound/NotFound';
import MyInfo from '@Component/Kayttaja/MyInfo';
import LandingPage from '@Component/Kayttaja/LandingPage';

export const buildRoutes = breadcrumbStore => ({
  '/': wrap(LandingPage, _ => {
    breadcrumbStore.set([]);
    return true;
  }),
  '/yritys': wrap(Yritys, _ => {
    breadcrumbStore.set([{ label: 'Yritykset', url: '/yritys' }]);
    return true;
  }),
  '/yritys/*': wrap(Yritys, _ => {
    breadcrumbStore.set([{ label: 'Yritykset', url: '/yritys' }]);
    return true;
  }),
  '/halytykset': wrap(Halytykset, _ => {
    breadcrumbStore.set([{ label: 'Halytykset', url: '/halytykset' }]);
    return true;
  }),
  '/kaytonvalvonta': wrap(Kaytonvalvonta, _ => {
    breadcrumbStore.set([{ label: 'Käytönvalvonta', url: '/kaytonvalvonta' }]);
    return true;
  }),
  '/tyojono': wrap(Tyojono, _ => {
    breadcrumbStore.set([{ label: 'Työjono', url: '/tyojono' }]);
    return true;
  }),
  '/kayttaja/*': wrap(Kayttaja, _ => {
    breadcrumbStore.set([{ label: 'Kayttaja', url: '/kayttaja' }]);
    return true;
  }),
  '/laatija/*': wrap(Laatija, _ => {
    breadcrumbStore.set([{ label: 'Laatija', url: '/laatija' }]);
    return true;
  }),
  '/viestit': wrap(Viestit, _ => {
    breadcrumbStore.set([{ label: 'Viestit', url: '/viestit' }]);
    return true;
  }),
  '/energiatodistus/*': wrap(Energiatodistus, _ => {
    breadcrumbStore.set([
      { label: 'Energiatodistukset', url: '/energiatodistus' }
    ]);
    return true;
  }),
  '/myinfo': wrap(MyInfo, _ => {
    breadcrumbStore.set([{ label: 'Omat tiedot', url: '/myinfo' }]);
    return true;
  }),
  '*': wrap(NotFound, _ => {
    breadcrumbStore.set([]);
    return true;
  })
});
