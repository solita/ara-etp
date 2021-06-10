<script>
  import * as R from 'ramda';
  import * as Kayttajat from '@Utility/kayttajat';
  import { _ } from '@Language/i18n';

  export let whoami;
  export let ketju;
  export let kasittelijat;
  export let icons = false;
</script>

<style>
</style>

{#if !Kayttajat.isLaatija(whoami)}
  {#each R.prop('kasittelija-id', ketju).toArray() as kasittelijaId}
    <div class="flex font-bold">
      {#if icons}<span class="font-icon">person</span>{/if}
      {#if R.eqProps('id', R.find(R.propEq('id', kasittelijaId), kasittelijat), whoami)}
        <span class="text-primary">
          {$_('viesti.ketju.existing.self')}
        </span>
      {:else}
        <span>
          {R.find(R.propEq('id', kasittelijaId), kasittelijat).etunimi}
        </span>
      {/if}
    </div>
  {:else}
    <div class="flex font-bold text-error">
      {#if icons}<span class="font-icon">error_outline</span>{/if}
      <span class="whitespace-no-wrap">
        {$_('viesti.ketju.existing.no-handler')}
      </span>
    </div>
  {/each}
{/if}
