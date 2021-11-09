<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import Autocomplete from '@Component/Autocomplete/Autocomplete';

  export let values = [];
  export let nameprefix;
  export let kunnat;

  let value = R.defaultTo('', R.head(values));

  const formatKunta = kunta => kunta['label-fi'];

  let input;

  let completedValue = R.compose(
    Maybe.orSome(''),
    R.map(formatKunta),
    Maybe.nullReturning(R.find(R.propEq('id', value)))
  )(kunnat);

  $: value = R.compose(
    Maybe.orSome(''),
    R.map(R.prop('id')),
    Maybe.nullReturning(
      R.find(R.compose(R.equals(completedValue), formatKunta))
    )
  )(kunnat);

  $: {
    input && (input.value = value);
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }
</script>

<style>
</style>

<Autocomplete
  bind:completedValue
  items={R.map(formatKunta, kunnat)}
  size={10000} />
<input
  bind:this={input}
  tabindex="-1"
  class="sr-only"
  name={`${nameprefix}_value_0`} />
