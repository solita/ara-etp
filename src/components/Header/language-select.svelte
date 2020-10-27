<script>
  import { _, locale } from '@Language/i18n';

  const changeLanguage = language => () => {
    localStorage.setItem('locale', language);
    window.location.reload();
  };
</script>

<style type="text/postcss">
  .active {
    @apply font-bold;
  }

  div {
    @apply font-light text-sm;
  }
</style>

<!-- purgecss: active -->
<div>
  <button
    class="hover:underline"
    on:click={changeLanguage('fi-FI')}
    class:active={$locale === 'fi' || $locale === 'fi-FI'}>
    Suomeksi
  </button>
  |
  <button
    class="hover:underline"
    on:click={changeLanguage('sv-FI')}
    class:active={$locale === 'sv' || $locale === 'sv-FI'}>
    På svenska
  </button>
</div>
