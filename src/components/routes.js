import Home from '@Component/Home/Home';
import Yritys from '@Component/Yritys/Yritys';
import NotFound from '@Component/NotFound/NotFound';

export const routes = {
  '/': Home,
  '/yritys': Yritys,
  '/yritys/*': Yritys,
  '*': NotFound
};
