import { writable } from 'svelte/store';
import * as R from 'ramda';
import * as Either from '@Utility/either-utils';

export const currentUserStore = writable();
export const errorStore = writable();
export const countryStore = writable(Either.Left('Not initialized'));
export const breadcrumbStore = writable([]);

const createFlashMessageStore = () => {
  const { subscribe, set, update } = writable([]);

  return {
    subscribe,
    add: R.curry((module, type, text) =>
      update(R.compose(R.uniq, R.append({ module, type, text })))
    ),
    remove: message => update(R.reject(R.equals(message))),
    flush: () => set([])
  };
};

export const flashMessageStore = createFlashMessageStore();
