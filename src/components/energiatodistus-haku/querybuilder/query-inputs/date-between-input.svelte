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
  export let startValue = R.compose(
    Either.orSome(dfns.startOfToday()),
    parsers.parseISODate,
    R.head
  )(values);

  export let endValue = R.compose(
    Either.orSome(dfns.startOfToday()),
    parsers.parseISODate,
    R.last
  )(values);

  let startInput;
  let startInputValue;
  let endInput;
  let endInputValue;

  $: {
    startInputValue = startValue.toISOString();
    endInputValue = endValue.toISOString();

    startInput &&
      startInput.dispatchEvent(new Event('change', { bubbles: true }));
    endInput && endInput.dispatchEvent(new Event('change', { bubbles: true }));
  }
</script>

<input
  bind:this={startInput}
  class="sr-only"
  name={`${nameprefix}_startValue_${index}`}
  value={startInputValue} />
<Datepicker bind:startValue />
<input
  bind:this={endInput}
  class="sr-only"
  name={`${nameprefix}_endValue_${index}`}
  value={endInputValue} />
<Datepicker bind:endValue />
