<script>
  import { slide } from 'svelte/transition';

  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import Perustiedot from './toimenpiteet-toimenpide-perustiedot.svelte';
  import ToimenpideEhdotukset from './toimenpiteet-toimenpide-ehdotukset.svelte';
  import EnergiankulutuksenMuutos from './toimenpiteet-energiankulutuksen-muutos.svelte';

  import H4 from '@Component/H/H4';
  const i18n = $_;

  export let energiatodistus;
  export let perusparannuspassi;
  export let pppSchema;
  export let vaihe;

  export let inputLanguage;

  export let toimenpideEhdotuksetLuokittelu;

  export let open = false;
  const toggleOpen = () => (open = R.not(open));
</script>

<div
  data-open={open}
  class="bg-background p-8 data-[open=false]:pb-6 data-[open=false]:pt-6">
  <div class="flex items-center justify-between">
    <H4
      text={i18n('perusparannuspassi.toimenpiteet.osio.header', {
        values: { vaihe: vaihe['vaihe-nro'] }
      })} />
    <button
      class="inline-flex p-3 pr-4 font-bold uppercase text-secondary hover:bg-althover"
      type="button"
      on:click={toggleOpen}>
      {#if R.equals(true, open)}
        <span class="material-icons">keyboard_arrow_up</span>
        <span class="truncate"
          >{i18n('perusparannuspassi.toimenpiteet.osio.sulje')}</span>
      {:else}
        <span class="material-icons">keyboard_arrow_down</span>
        <span class="truncate"
          >{i18n('perusparannuspassi.toimenpiteet.osio.avaa')}</span>
      {/if}
    </button>
  </div>
  {#if R.equals(true, open)}
    <div transition:slide={{ duration: 200 }} class="grid gap-6">
      <Perustiedot {perusparannuspassi} {pppSchema} {vaihe} {energiatodistus} />
      <ToimenpideEhdotukset
        bind:perusparannuspassi
        {pppSchema}
        {vaihe}
        {inputLanguage}
        {toimenpideEhdotuksetLuokittelu} />
      <EnergiankulutuksenMuutos
        {perusparannuspassi}
        {energiatodistus}
        {vaihe} />
    </div>
  {/if}
</div>
