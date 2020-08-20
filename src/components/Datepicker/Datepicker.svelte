<script>
  import { onMount } from 'svelte';

  import { locale } from '@Language/i18n';
  import * as formats from '@Utility/formats';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as R from 'ramda';
  import * as dfns from 'date-fns';

  import { litepicker } from './litepicker';
  import Litepicker from 'litepicker';
  import SimpleInput from '@Component/Input/SimpleInput';
  import SquareInputWrapper from '@Component/Input/SquareInputWrapper';

  export let start = dfns.formatISO(new Date());
  export let end = dfns.formatISO(new Date());
  export let range = false;
  export let update = () => {};

  let container;
  let destroy;

  $: update(start);

  onMount(() => {
    const inputs = container.querySelectorAll('input');

    const options = {
      element: R.head(inputs),
      format: 'D.M.YYYY',
      singleMode: !range,
      onSelect: range
        ? (startDate, endDate) => {
            start = dfns.formatISO(startDate);
            end = dfns.formatISO(endDate);
          }
        : startDate => {
            start = dfns.formatISO(startDate);
          },
      lang: $locale,
      dropdowns: { minYear: 1970, maxYear: null, months: true, years: true }
    };

    const lastElement =
      R.length(inputs) > 1 ? { elementEnd: R.last(inputs) } : {};

    const picker = new Litepicker(R.mergeRight(lastElement, options));

    return () => picker.destroy();
  });
</script>

<style>
  input {
    @apply w-full relative font-medium py-1;
  }

  input:focus {
    @apply outline-none;
  }

  input:hover {
    @apply bg-background;
  }

  input:disabled {
    @apply bg-light;
  }
</style>

<div bind:this={container}>
  <SquareInputWrapper>
    <input
      class="text-center"
      value={dfns.format(dfns.parseISO(start), 'd.M.yyyy')}
      on:focus={evt => evt.target.click()} />
  </SquareInputWrapper>
  {#if range}
    <SquareInputWrapper>
      <input
        class="text-center"
        value={dfns.format(dfns.parseISO(end), 'd.M.yyyy')}
        on:focus={evt => evt.target.click()} />
    </SquareInputWrapper>
  {/if}
</div>
