import {init, locale, _, locales, register, isLoading} from 'svelte-i18n';

const fetchLocale = locale => {
  const localeUrl = `language/${locale}.json`;
  return fetch(localeUrl).then(response => response.json());
}

const setupI18n = () => {
  locale.set('fi');

  register('fi', () => fetchLocale('fi'));
  register('sv', () => fetchLocale('sv'));
  register('en', () => fetchLocale('en'));

  init({
    fallbackLocale: 'fi',
    initialLocale: {
      navigator: true
    }
  });
}

export { setupI18n, _, locale, locales, isLoading};