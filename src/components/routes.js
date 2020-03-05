import NewYritys from '@Component/Yritys/NewYritys';
import ExistingYritys from '@Component/Yritys/ExistingYritys';

export const routes = {
  '/yritys/new': NewYritys,
  '/yritys/:id': ExistingYritys
};
