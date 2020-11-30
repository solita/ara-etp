<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as dfns from 'date-fns';
  import * as parsers from '@Utility/parsers';
  import * as Either from '@Utility/either-utils';
  import Datepicker from '@Component/Datepicker/Datepicker';

  export let values;
  export let nameprefix;
  export let index = 0;
  export let value = R.compose(
    Either.orSome(dfns.startOfToday()),
    parsers.parseISODate,
    R.head
  )(values);

  let input;
  let inputValue;

  $: {
    inputValue = value.toISOString();
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }
</script>

<input
  bind:this={input}
  class="sr-only"
  name={`${nameprefix}_value_${index}`}
  value={inputValue} />
<Datepicker bind:value />
