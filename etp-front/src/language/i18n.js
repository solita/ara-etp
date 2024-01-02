import {
  init,
  locale,
  _,
  locales,
  addMessages,
  getLocaleFromNavigator
} from 'svelte-i18n';

import fi from './fi.json';
import sv from './sv.json';
import { shortLocale } from '@Language/locale-utils';

const setupI18n = () => {
  addMessages('fi-FI', fi);
  addMessages('fi', fi);
  addMessages('sv-FI', sv);
  addMessages('sv', sv);

  const safeNavigatorLocale = ['fi', 'sv'].includes(
    shortLocale(getLocaleFromNavigator())
  )
    ? getLocaleFromNavigator()
    : 'fi';

  init({
    fallbackLocale: 'fi-FI',
    initialLocale: localStorage.getItem('locale') || safeNavigatorLocale
  });
};

/**
 * @global
 * @typedef {Function} Translate
 * @sig string -> string
 *
 * @description Translates the given string according to locale
 */

/**
 * @global
 * @typedef {Function} Locale
 * @sig () -> string
 *
 * @description Returns the current locale
 */

export { setupI18n, _, locale, locales };
