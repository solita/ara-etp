import { writable } from 'svelte/store';
import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

export const currentUserStore = writable(Maybe.None());
export const errorStore = writable();
export const countryStore = writable(Either.Left('Not initialized'));
export const patevyystasoStore = writable(Either.Left('Not initialized'));
export const patevyydetStore = writable(Either.Left('Not initialized'));
export const toimintaAlueetStore = writable(Either.Left('Not initialized'));

const createIdTranslateStore = () => {
  const { subscribe, update } = writable({
    yritys: { all: 'navigation.yritykset', new: 'yritys.uusi-yritys' },
    kayttaja: { all: 'navigation.kayttajat' },
    energiatodistus: { new: 'navigation.uusi-energiatodistus' }
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
      )
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

      return update(R.compose(R.uniq, R.append(message)));
    }),
    addPersist: R.curry((module, type, text) => {
      const message = { module, type, text, persist: false };

      if (type === 'success') {
        setTimeout(
          () => update(R.reject(R.eqBy(R.omit('persist'), message))),
          5000
        );
      }
      return update(R.compose(R.uniq, R.append(message)));
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
