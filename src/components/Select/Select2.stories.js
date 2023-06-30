import * as R from 'ramda';
import Select2 from './Select2';
import * as Maybe from '@Utility/maybe-utils';
import { _ } from '@Language/i18n';
import { get } from 'svelte/store';

export default { title: 'Select2' };

export const withItems = () => ({
  Component: Select2,
  props: {
    items: R.map(Maybe.Some, [1, 2, 3, 4, 5, 6, 7, 8, 9, 0]),
    model: { selected: Maybe.None() },
    lens: R.lensProp('selected'),
    format: Maybe.fold(get(_)('validation.no-selection'), R.identity)
  }
});

export const withSelected = () => ({
  Component: Select2,
  props: {
    items: [1, 2, 3, 4, 5, 6, 7, 8, 9, 0],
    model: { selected: 1 },
    lens: R.lensProp('selected'),
    format: R.identity
  }
});

export const withObjects = () => ({
  Component: Select2,
  props: {
    items: R.map(Maybe.Some, [
      { id: 1, label: 'yksi' },
      { id: 2, label: 'kaksi' },
      { id: 3, label: 'kolme' },
      { id: 4, label: 'neljä' },
      { id: 5, label: 'viisi' }
    ]),
    model: { selected: Maybe.None() },
    lens: R.lensProp('selected'),
    format: Maybe.fold(get(_)('validation.no-selection'), R.prop('label'))
  }
});
