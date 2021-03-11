import { writable } from 'svelte/store';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

const createIdTranslateStore = () => {
  const { subscribe, update } = writable({
    yritys: {},
    kayttaja: {},
    energiatodistus: {},
    viesti: {}
  });

  return {
    subscribe,
    update,
    updateYritys: yritys =>
      update(
        R.assocPath(['yritys', R.prop('id', yritys)], R.prop('nimi', yritys))
      ),
    updateKayttaja: kayttaja =>
      update(
        R.assocPath(
          ['kayttaja', R.prop('id', kayttaja)],
          R.pick(['etunimi', 'sukunimi', 'id', 'rooli'], kayttaja)
        )
      ),
    updateKetju: ketju => {
      update(
        R.assocPath(
          ['viesti', R.prop('id', ketju)],
          Maybe.fromNull(R.prop('energiatodistus-id', ketju))
        )
      );
    }
  };
};

export const idTranslateStore = createIdTranslateStore();

const createFlashMessageStore = () => {
  const { subscribe, set, update } = writable([]);

  return {
    subscribe,
    add: R.curry((module, type, text) => {
      const message = { module, type, text, persist: false };

      if (type === 'success') {
        setTimeout(
          () => update(R.reject(R.eqBy(R.omit('persist'), message))),
          5000
        );
      }

      return set([message]);
    }),
    addPersist: R.curry((module, type, text) => {
      const message = { module, type, text, persist: false };

      if (type === 'success') {
        setTimeout(
          () => update(R.reject(R.eqBy(R.omit('persist'), message))),
          5000
        );
      }
      return set([message]);
    }),
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
