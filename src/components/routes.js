import Home from '@Component/Home/Home';
import Yritys from '@Component/Yritys/Yritys';
import Laatija from '@Component/Laatija/Laatija';
import EnergiaTodistus from '@Component/EnergiaTodistus/EnergiaTodistus';
import NotFound from '@Component/NotFound/NotFound';
import MyInfo from '@Component/Kayttaja/MyInfo';

export const routes = {
  '/': Home,
  '/yritys': Yritys,
  '/yritys/*': Yritys,
  '/laatija/*': Laatija,
  '/energiatodistus/*': EnergiaTodistus,
  '/myinfo': MyInfo,
  '*': NotFound
};
