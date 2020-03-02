import NewYritys from './Yritys/NewYritys';
import ExistingYritys from './Yritys/ExistingYritys';

export const routes = {
  '/yritys/new': NewYritys,
  '/yritys/:id': ExistingYritys
};
