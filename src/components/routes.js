import { wrap } from 'svelte-spa-router';
import * as R from 'ramda';
import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';

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

export const buildRoutes = currentUser => ({
  '/': LandingPage,
  '/yritys/*': Yritys,
  '/halytykset': wrap(Halytykset, _ =>
    KayttajaUtils.kayttajaHasAccessToResource(
      [KayttajaUtils.paakayttajaRole],
      currentUser
    )
  ),
  '/kaytonvalvonta': wrap(Kaytonvalvonta, _ =>
    KayttajaUtils.kayttajaHasAccessToResource(
      [KayttajaUtils.paakayttajaRole],
      currentUser
    )
  ),
  '/tyojono': wrap(Tyojono, _ =>
    KayttajaUtils.kayttajaHasAccessToResource(
      [KayttajaUtils.paakayttajaRole],
      currentUser
    )
  ),
  '/kayttaja/*': Kayttaja,
  '/laatija/*': Laatija,
  '/viestit': Viestit,
  '/energiatodistus/*': wrap(Energiatodistus, _ =>
    KayttajaUtils.kayttajaHasAccessToResource(
      [KayttajaUtils.laatijaRole, KayttajaUtils.paakayttajaRole],
      currentUser
    )
  ),
  '/myinfo': MyInfo,
  '*': NotFound
});
