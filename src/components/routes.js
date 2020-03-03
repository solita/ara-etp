import NewYritys from '/components/Yritys/NewYritys';
import ExistingYritys from '/components/Yritys/ExistingYritys';

export const routes = {
  '/yritys/new': NewYritys,
  '/yritys/:id': ExistingYritys
};
