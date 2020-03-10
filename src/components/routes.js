import Home from '@Component/Home/Home';
import NewYritys from '@Component/Yritys/NewYritys';
import ExistingYritys from '@Component/Yritys/ExistingYritys';
import NotFound from '@Component/NotFound/NotFound';

export const routes = {
  '/': Home,
  '/yritys/new': NewYritys,
  '/yritys/:id': ExistingYritys,
  '*': NotFound
};
