import { wrap } from 'svelte-spa-router/wrap';

import Yritys from '@Component/Yritys/Yritys';
import Kayttaja from '@Component/Kayttaja/Kayttaja';
import Viesti from '@Component/viesti/viesti';
import Laatija from '@Component/Laatija/Laatija';
import Halytykset from '@Component/Halytykset/Halytykset';
import Kaytonvalvonta from '@Component/Kaytonvalvonta/Kaytonvalvonta';
import Tyojono from '@Component/Tyojono/Tyojono';
import Energiatodistus from '@Component/Energiatodistus/Energiatodistus';
import Valvonta from '@Component/valvonta-oikeellisuus/router';
import NotFound from '@Component/NotFound/NotFound';
import MyInfo from '@Component/Kayttaja/MyInfo';
import LandingPage from '@Component/Kayttaja/LandingPage';

export const buildRoutes = currentUser => ({
  '/': LandingPage,
  '/yritys/*': Yritys,
  '/halytykset': Halytykset,
  '/kaytonvalvonta': Kaytonvalvonta,
  '/tyojono': Tyojono,
  '/kayttaja/*': Kayttaja,
  '/laatija/*': Laatija,
  '/viesti/*': Viesti,
  '/energiatodistus/*': Energiatodistus,
  '/valvonta/oikeellisuus/*': Valvonta,
  '/myinfo': wrap({
    component: MyInfo,
    props: {
      whoami: currentUser
    }
  }),
  '*': NotFound
});
