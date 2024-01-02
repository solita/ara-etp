<script context="module">
  import { writable, derived } from 'svelte/store';
  import sv from '@Asset/sv.json';
  import fi from '@Asset/fi.json';

  const translations = { fi, sv };

  const navigatorLanguage = navigator.language.split('-')[0];

  export const locale = writable(
    localStorage.getItem('locale') ??
      (['fi', 'sv'].includes(navigatorLanguage) ? navigatorLanguage : 'fi')
  );

  export const _ = derived(
    locale,
    $locale => str => translations[$locale][str] || str
  );

  export const labelLocale = (locale, obj) => obj?.[`label-${locale}`];

  export const setLocale = l => {
    localStorage.setItem('locale', l);
    locale.set(l);
  };
</script>

<div lang={$locale}>
  <slot />
</div>
