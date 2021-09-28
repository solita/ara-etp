import { wrap } from 'svelte-spa-router/wrap';

import Yritys from '@Pages/yritys';
import Kayttaja from '@Pages/kayttaja/Kayttaja';
import Viesti from '@Pages/viesti';
import Ohje from '@Pages/ohje';
import Laatija from '@Pages/laatija';
import Energiatodistus from '@Pages/energiatodistus';
import ValvontaOikeellisuus from '@Pages/valvonta-oikeellisuus';
import ValvontaKaytto from '@Pages/valvonta-kaytto';
import NotFound from '@Pages/not-found/not-found';
import MyInfo from '@Pages/kayttaja/MyInfo';
import LandingPage from '@Pages/kayttaja/LandingPage';

export const buildRoutes = currentUser => ({
  '/': LandingPage,
  '/yritys/*': Yritys,
  '/kayttaja/*': Kayttaja,
  '/laatija/*': Laatija,
  '/viesti/*': Viesti,
  '/ohje/*': Ohje,
  '/energiatodistus/*': Energiatodistus,
  '/valvonta/oikeellisuus/*': ValvontaOikeellisuus,
  '/valvonta/kaytto/*': ValvontaKaytto,
  '/myinfo': wrap({
    component: MyInfo,
    props: {
      whoami: currentUser
    }
  }),
  '*': NotFound
});
