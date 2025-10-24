<script>
  import H3 from '@Component/H/H3';
  import { _ } from '@Language/i18n';

  import Vaiheistus from './laskennan-tulokset-vaiheistus.svelte';
  import LaskennallinenOstoenergia from './laskennan-tulokset-laskennallinen-ostoenergia.svelte';
  import UusiutuvaEnergia from './laskennan-tulokset-uusiutuva-energia.svelte';
  import ToteutunutOstoenergia from './laskennan-tulokset-toteutunut-ostoenergia.svelte';

  export let energiatodistus;
  export let perusparannuspassi;
  export let schema;
</script>

<div class="flex flex-col gap-6">
  <H3 text={$_('perusparannuspassi.laskennan-tulokset.header')} />

  <Vaiheistus bind:perusparannuspassi {schema} />
  <LaskennallinenOstoenergia
    bind:perusparannuspassi
    {energiatodistus}
    {schema} />
  <UusiutuvaEnergia bind:perusparannuspassi {energiatodistus} {schema} />
  <ToteutunutOstoenergia bind:perusparannuspassi {energiatodistus} {schema} />
</div>
