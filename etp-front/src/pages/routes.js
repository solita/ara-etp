import { wrap } from 'svelte-spa-router/wrap';

import Yritys from '@Pages/yritys';
import Kayttaja from '@Pages/kayttaja';
import Viesti from '@Pages/viesti';
import Ohje from '@Pages/ohje';
import Laatija from '@Pages/laatija';
import Aineistoasiakas from '@Pages/aineistoasiakas';
import Energiatodistus from '@Pages/energiatodistus';
import ValvontaOikeellisuus from '@Pages/valvonta-oikeellisuus';
import ValvontaKaytto from '@Pages/valvonta-kaytto';
import NotFound from '@Pages/not-found/not-found';
import MyInfo from '@Pages/kayttaja/my-info';
import LandingPage from '@Pages/kayttaja/landing-page';
import Saavutettavuusseloste from '@Pages/saavutettavuusseloste';
import EnergiatodistusrekisteriTietosuojaseloste from '@Pages/tietosuojaseloste/energiatodistusrekisteri';

export const buildRoutes = currentUser => ({
  '/': LandingPage,
  '/yritys/*': Yritys,
  '/kayttaja/*': Kayttaja,
  '/laatija/*': Laatija,
  '/aineistoasiakas/*': Aineistoasiakas,
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
  '/saavutettavuusseloste': Saavutettavuusseloste,
  '/tietosuojaseloste-energiatodistusrekisteri':
    EnergiatodistusrekisteriTietosuojaseloste,
  '*': NotFound
});
