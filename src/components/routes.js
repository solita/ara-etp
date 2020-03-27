import Home from '@Component/Home/Home';
import Yritys from '@Component/Yritys/Yritys';
import Laatija from '@Component/Laatija/Laatija';
import EnergiaTodistus from '@Component/EnergiaTodistus/EnergiaTodistus';
import NotFound from '@Component/NotFound/NotFound';

export const routes = {
  '/': Home,
  '/yritys': Yritys,
  '/yritys/*': Yritys,
  '/laatija/*': Laatija,
  '/energiatodistus/*': EnergiaTodistus,
  '*': NotFound
};
