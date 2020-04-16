import Yritys from '@Component/Yritys/Yritys';
import Kayttaja from '@Component/Kayttaja/Kayttaja';
import Viestit from '@Component/Viestit/Viestit';
import Laatija from '@Component/Laatija/Laatija';
import Halytykset from '@Component/Halytykset/Halytykset';
import Kaytonvalvonta from '@Component/Kaytonvalvonta/Kaytonvalvonta';
import Tyojono from '@Component/Tyojono/Tyojono';
import EnergiaTodistus from '@Component/EnergiaTodistus/EnergiaTodistus';
import NotFound from '@Component/NotFound/NotFound';
import MyInfo from '@Component/Kayttaja/MyInfo';
import LandingPage from '@Component/Kayttaja/LandingPage';

export const routes = {
  '/': LandingPage,
  '/yritys': Yritys,
  '/yritys/*': Yritys,
  '/halytykset': Halytykset,
  '/kaytonvalvonta': Kaytonvalvonta,
  '/tyojono': Tyojono,
  '/kayttaja/*': Kayttaja,
  '/laatija/*': Laatija,
  '/viestit': Viestit,
  '/energiatodistus/*': EnergiaTodistus,
  '/myinfo': MyInfo,
  '*': NotFound
};
