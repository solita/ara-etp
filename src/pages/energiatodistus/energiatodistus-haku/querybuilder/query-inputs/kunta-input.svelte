<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import Autocomplete from '@Component/Autocomplete/Autocomplete';

  import { locale } from '@Language/i18n';
  import { label } from '@Language/locale-utils';

  export let values = [];
  export let nameprefix;
  export let kunnat;

  let value = R.defaultTo('', R.head(values));
  let input;

  const formatKunta = label; // formatKunta: locale -> item -> label

  const completedValueWithLocale = locale =>
    R.compose(
      Maybe.orSome(''),
      R.map(formatKunta(locale)),
      Maybe.nullReturning(R.find(R.propEq('id', value)))
    )(kunnat);

  $: value = R.compose(
    Maybe.orSome(''),
    R.map(R.prop('id')),
    Maybe.nullReturning(
      R.find(
        R.compose(
          R.equals(completedValueWithLocale($locale)),
          formatKunta($locale)
        )
      )
    )
  )(kunnat);

  $: {
    input && (input.value = value);
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }
  $: completedValue = completedValueWithLocale($locale);
</script>

<style>
</style>

<Autocomplete
  bind:completedValue
  items={R.map(formatKunta($locale), kunnat)}
  size={10000} />
<input
  bind:this={input}
  tabindex="-1"
  class="sr-only"
  name={`${nameprefix}_value_0`} />
