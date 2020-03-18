import Home from '@Component/Home/Home';
import Yritys from '@Component/Yritys/Yritys';
import NotFound from '@Component/NotFound/NotFound';
import Laatija from '@Component/Laatija/Laatija';

export const routes = {
  '/': Home,
  '/yritys': Yritys,
  '/yritys/*': Yritys,
  '/laatija': Laatija,
  '/laatija/*': Laatija,
  '*': NotFound
};
