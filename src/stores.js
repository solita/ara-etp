import { writable } from 'svelte/store';
import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

/* deprecated - do not use */
export const currentUserStore = writable(Maybe.None());
/* deprecated - do not use */
export const patevyystasoStore = writable(Either.Left('Not initialized'));

const createIdTranslateStore = () => {
  const { subscribe, update } = writable({
    yritys: { all: 'navigation.yritykset', new: 'yritys.uusi-yritys' },
    kayttaja: { all: 'navigation.kayttajat' },
    energiatodistus: { new: 'navigation.uusi-energiatodistus' },
    viesti: { all: 'navigation.viesti' }
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
          R.compose(
            R.join(' '),
            R.converge(Array.of, [R.prop('etunimi'), R.prop('sukunimi')])
          )(kayttaja)
        )
      ),
    updateKetju: ketju => {
      debugger;
      if (ketju['energiatodistus-id'] && ketju['energiatodistus-versio']) {
        update(
          R.assocPath(['viesti', R.prop('id', ketju)], {
            id: R.prop('energiatodistus-id', ketju),
            versio: R.prop('energiatodistus-versio', ketju)
          })
        );
      }
    }
  };
};

export const idTranslateStore = createIdTranslateStore();

window.idTranslateStore = idTranslateStore;

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
