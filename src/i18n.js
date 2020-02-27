import {
  init,
  locale,
  _,
  locales,
  addMessages,
  getLocaleFromNavigator
} from 'svelte-i18n';

import fi from '../public/language/fi.json';
import sv from '../public/language/sv.json';

const setupI18n = () => {
  addMessages('fi-FI', fi);
  addMessages('fi', fi);
  addMessages('sv-FI', sv);
  addMessages('sv', sv);

  init({
    fallbackLocale: 'fi-FI',
    initialLocale: getLocaleFromNavigator()
  });
};

export { setupI18n, _, locale, locales };
