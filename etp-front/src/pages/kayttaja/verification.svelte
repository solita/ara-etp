<script>
  import * as Kayttajat from '@Utility/kayttajat';
  import { _ } from '@Language/i18n';

  const i18n = $_;
  const i18nRoot = 'kayttaja.verification';

  export let whoami;
  export let kayttaja;
</script>

<style type="text/postcss">
  h1 {
    @apply text-sm font-bold uppercase text-primary;
  }
  h1 span {
    @apply text-lg font-normal lowercase;
  }
</style>

{#if Kayttajat.isVerificationActive(whoami, kayttaja)}
  <div class="flex">
    <div class="my-4 rounded-md border-2 border-primary/15 p-4 shadow-md">
      <h1>
        <span class="font-icon">info_outline</span>
        {i18n(i18nRoot + '.header')}
      </h1>
      <p class="my-2">{i18n(i18nRoot + '.p-1')}</p>
      <p class="my-2">{i18n(i18nRoot + '.p-2')}</p>
      <p class="my-2">{i18n(i18nRoot + '.p-3')}</p>
    </div>
  </div>
{/if}
