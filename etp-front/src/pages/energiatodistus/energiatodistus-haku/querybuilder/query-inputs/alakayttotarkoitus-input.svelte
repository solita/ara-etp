<script>
  import * as R from 'ramda';

  import VersioLuokkaInput from './versioluokka-input';

  export let values;
  export let nameprefix;
  export let luokittelut;
  export let key = 'alakayttotarkoitusluokat';

  let [versio, luokittelu] = values;

  let previousVersio = versio;

  let input;

  if (luokittelu === '') {
    luokittelu = R.compose(
      R.prop('id'),
      R.head,
      R.path([versio, key])
    )(luokittelut);
  }

  $: {
    input && (input.value = luokittelu);
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }

  $: if (versio !== previousVersio) {
    luokittelu = R.compose(
      R.prop('id'),
      R.head,
      R.path([versio, key])
    )(luokittelut);
    input && (input.value = luokittelu);
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
    previousVersio = versio;
  }
</script>

<input bind:value={versio} class="hidden" name={`${nameprefix}_value_0`} />
<input bind:this={input} class="hidden" name={`${nameprefix}_value_1`} />

<VersioLuokkaInput
  bind:versio
  bind:luokittelu
  {values}
  {luokittelut}
  {nameprefix}
  {key} />
