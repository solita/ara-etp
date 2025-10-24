<script>
  import * as R from 'ramda';

  import H3 from '@Component/H/H3';
  import HR from '@Component/HR/HR';
  import { _ } from '@Language/i18n';
  import Osio from './toimenpiteet-osio';

  const i18n = $_;

  export let perusparannuspassi;

  export let etSchema;
  export let energiatodistus;
  export let pppSchema;
  export let toimenpideEhdotuksetLuokittelu;
  export let inputLanguage;

  $: console.log(perusparannuspassi);
</script>

<H3 text={i18n(`perusparannuspassi.vaiheet.0.toimenpiteet.header`)} />

<div class="flex min-w-full flex-col gap-y-8 overflow-x-auto">
  {#each perusparannuspassi.vaiheet as vaihe}
    {#if R.equals(vaihe.valid, true)}
      <Osio
        {energiatodistus}
        {etSchema}
        bind:perusparannuspassi
        {pppSchema}
        {toimenpideEhdotuksetLuokittelu}
        {vaihe}
        {inputLanguage} />
    {/if}
  {/each}
</div>
