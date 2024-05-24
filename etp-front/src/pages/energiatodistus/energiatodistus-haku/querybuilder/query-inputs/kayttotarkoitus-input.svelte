<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import VersioLuokkaInput from './versioluokka-input';

  export let values;
  export let nameprefix;
  export let luokittelut;
  export let key = 'kayttotarkoitusluokat';

  let [versio, luokittelu] = values;

  let previousVersio = versio;

  let input;

  const currentLuokittelu = (luokittelut, versio, luokittelu) =>
    R.compose(
      Maybe.orSome(
        R.compose(R.prop('id'), R.head, R.path([versio, key]))(luokittelut)
      ),
      R.map(R.prop('kayttotarkoitusluokka-id')),
      R.chain(
        Maybe.nullReturning(
          R.find(R.propEq(R.compose(R.head, R.split(','))(luokittelu), 'id'))
        )
      ),
      R.chain(
        Maybe.nullReturning(R.path([versio, 'alakayttotarkoitusluokat']))
      ),
      Maybe.Some
    )(luokittelut);

  luokittelu = currentLuokittelu(luokittelut, versio, luokittelu);

  const currentLuokittelut = (luokittelut, versio, luokittelu) =>
    R.compose(
      R.pluck('id'),
      R.filter(R.propEq(luokittelu, 'kayttotarkoitusluokka-id')),
      R.path([versio, 'alakayttotarkoitusluokat'])
    )(luokittelut);

  $: {
    input &&
      (input.value = currentLuokittelut(luokittelut, versio, luokittelu));
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }

  $: if (versio !== previousVersio) {
    luokittelu = R.compose(
      R.prop('id'),
      R.head,
      R.path([versio, key])
    )(luokittelut);
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
