import NewYritys from './Yritys/NewYritys.svelte';
import ExistingYritys from './Yritys/ExistingYritys.svelte';

export const routes = {
  '/yritys/new': NewYritys,
  '/yritys/:id': ExistingYritys
};
