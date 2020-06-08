<script>
  import { _ } from '@Language/i18n';

  import H2 from '@Component/H/H2';
  import H3 from '@Component/H/H3';

  import Textarea from '@Component/Energiatodistus/Textarea';

  export let disabled;
  export let schema;
  export let energiatodistus;
  export let versio;
</script>

<style>

</style>

<H2 text={$_(`energiatodistus.perustiedot.keskeiset-suositukset-heading.${versio}.h2`)} />
<H3 text={$_(`energiatodistus.perustiedot.keskeiset-suositukset-heading.${versio}.h3`)}  />

<div class="w-full py-4 mb-4">
  <Textarea
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['perustiedot', 'keskeiset-suositukset-fi']} />
</div>
