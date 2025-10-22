<script>
  import H3 from '@Component/H/H3';
  import { _ } from '@Language/i18n';

  import Vaiheistus from './ppp-laskennan-tulokset-vaiheistus';
  import LaskennallinenOstoenergia from './ppp-laskennan-tulokset-laskennallinen-ostoenergia';
  import UusiutuvaEnergia from './ppp-laskennan-tulokset-uusiutuva-energia';
  import ToteutunutOstoenergia from './ppp-laskennan-tulokset-toteutunut-ostoenergia';

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
