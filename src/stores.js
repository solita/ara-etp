import { writable } from 'svelte/store';
import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

export const currentUserStore = writable(Maybe.None());
export const errorStore = writable();
export const countryStore = writable(Either.Left('Not initialized'));
export const patevyystasoStore = writable(Either.Left('Not initialized'));
export const toimintaAlueetStore = writable(Either.Left('Not initialized'));
export const breadcrumbStore = writable([]);

const createNavigationStore = () => {
  const { subscribe, set, update } = writable([]);

  return {
    subscribe,
    set,
    replaceFirst: newFirst => update(R.compose(R.prepend(newFirst), R.tail))
  };
};

export const navigationStore = createNavigationStore();

const createFlashMessageStore = () => {
  const { subscribe, set, update } = writable([]);

  return {
    subscribe,
    add: R.curry((module, type, text) =>
      update(
        R.compose(R.uniq, R.append({ module, type, text, persist: false }))
      )
    ),
    addPersist: R.curry((module, type, text) =>
      update(R.compose(R.uniq, R.append({ module, type, text, persist: true })))
    ),
    remove: message => update(R.reject(R.equals(message))),
    flush: module =>
      update(
        R.compose(
          R.map(
            R.when(
              R.compose(R.equals(module), R.prop('module')),
              R.assoc('persist', false)
            )
          ),
          R.reject(
            R.allPass([
              R.compose(R.equals(module), R.prop('module')),
              R.compose(R.not, R.prop('persist'))
            ])
          )
        )
      )
  };
};

export const flashMessageStore = createFlashMessageStore();
