<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe.js';

  import H3 from '@Component/H/H3';
  import { _ } from '@Language/i18n';
  import Osio from './toimenpiteet-osio';

  const i18n = $_;

  export let energiatodistus;
  export let eTehokkuus = Maybe.None();
  export let perusparannuspassi;
  export let pppSchema;
  export let luokittelut;
  export let inputLanguage;
  export let disabled = false;
</script>

<H3 text={i18n(`perusparannuspassi.toimenpiteet.header`)} />

<div class="flex min-w-full flex-col gap-y-8 overflow-x-auto">
  {#each perusparannuspassi.vaiheet as vaihe}
    {#if R.compose(Maybe.isSome, EM.toMaybe, R.view(R.lensPath( ['tulokset', 'vaiheen-alku-pvm'] )))(vaihe)}
      <Osio
        {disabled}
        {energiatodistus}
        {eTehokkuus}
        bind:perusparannuspassi
        {pppSchema}
        {luokittelut}
        {vaihe}
        {inputLanguage} />
    {/if}
  {/each}
</div>
