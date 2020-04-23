<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import { currentUserStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import Select from '@Component/Select/Select';
  import H1 from '@Component/H/H1';

  import Laskutustieto from './Laskutustieto';

  export let params;

  const lens = R.lensProp('laskutustiedot');

  let model = {
    laskutustiedot: Maybe.None()
  };

  const laskutustiedot = [
    { id: 0, nimi: 'Henkilökohtaiset tiedot' },
    { id: 1, nimi: 'Yritys Y', ytunnus: '1234567-1' }
  ];

  $: formatLaskutustieto = R.prop('nimi');

  $: parseLaskutustieto = Maybe.fromNull;

  $: console.log(model);
</script>

<style>

</style>

<H1 text={$_('energiatodistus.allekirjoitus.laskutusosoite')} />
<Select
  items={laskutustiedot}
  bind:model
  {lens}
  format={formatLaskutustieto}
  parse={parseLaskutustieto}
  label={$_('energiatodistus.allekirjoitus.valitse-laskutusosoite')} />

<form on:submit|preventDefault>
  {#if R.compose( Maybe.isSome, R.view(lens) )(model)}
    <Laskutustieto
      laskutustieto={R.compose( Maybe.get, R.view(lens) )(model)} />
  {/if}
</form>
