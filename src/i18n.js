import {
  init,
  locale,
  _,
  locales,
  register,
  isLoading,
  getLocaleFromNavigator
} from 'svelte-i18n';

const fetchLocale = locale => {
  const localeUrl = `language/${locale}.json`;
  return fetch(localeUrl).then(response => response.json());
};

const setupI18n = () => {
  register('fi', () => fetchLocale('fi'));
  register('sv', () => fetchLocale('sv'));
  register('en', () => fetchLocale('en'));

  init({
    fallbackLocale: 'fi',
    initialLocale: getLocaleFromNavigator()
  });
};

export { setupI18n, _, locale, locales, isLoading };
