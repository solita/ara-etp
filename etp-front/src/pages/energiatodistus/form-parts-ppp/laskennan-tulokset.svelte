<script>
  import H3 from '@Component/H/H3';
  import { _ } from '@Language/i18n';

  import LaskennanTuloksetVaiheistus from './laskennan-tulokset-vaiheistus.svelte';
  import LaskennanTuloksetLaskennallinenOstoenergia from './laskennan-tulokset-laskennallinen-ostoenergia.svelte';
  import LaskennanTuloksetUusiutuvaEnergia from './laskennan-tulokset-uusiutuva-energia.svelte';
  import LaskennanTuloksetToteutunutOstoenergia from './laskennan-tulokset-toteutunut-ostoenergia.svelte';

  export let energiatodistus;
  export let perusparannuspassi;
  export let schema;
</script>

<div class="flex flex-col gap-6">
  <H3 text={$_('perusparannuspassi.laskennan-tulokset.header')} />

  <LaskennanTuloksetVaiheistus bind:perusparannuspassi {schema} />
  <LaskennanTuloksetLaskennallinenOstoenergia
    bind:perusparannuspassi
    {energiatodistus}
    {schema} />
  <LaskennanTuloksetUusiutuvaEnergia bind:perusparannuspassi {energiatodistus} {schema} />
  <LaskennanTuloksetToteutunutOstoenergia bind:perusparannuspassi {energiatodistus} {schema} />
</div>
