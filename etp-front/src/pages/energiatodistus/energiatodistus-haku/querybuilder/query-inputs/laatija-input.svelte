<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import Autocomplete from '@Component/Autocomplete/Autocomplete';

  export let values = [];
  export let nameprefix;
  export let laatijat;

  let value = R.defaultTo('', R.head(values));

  const formatLaatija = laatija =>
    `${laatija.etunimi} ${laatija.sukunimi} | ${laatija.email}`;

  let input;

  let completedValue = R.compose(
    Maybe.orSome(''),
    R.map(formatLaatija),
    Maybe.nullReturning(R.find(R.propEq(value, 'id')))
  )(laatijat);

  $: value = R.compose(
    Maybe.orSome(''),
    R.map(R.prop('id')),
    Maybe.nullReturning(
      R.find(R.compose(R.equals(completedValue), formatLaatija))
    )
  )(laatijat);

  $: {
    input && (input.value = value);
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }
</script>

<style>
</style>

<Autocomplete
  bind:completedValue
  items={R.map(formatLaatija, laatijat)}
  size={10000} />
<input
  bind:this={input}
  tabindex="-1"
  class="hidden"
  name={`${nameprefix}_value_0`} />
