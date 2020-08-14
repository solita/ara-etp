<script>
  import { locale } from '@Language/i18n';
  import * as formats from '@Utility/formats';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as R from 'ramda';
  import * as dfns from 'date-fns';

  import { litepicker } from './litepicker';
  import SimpleInput from '@Component/Input/SimpleInput';
  import SquareInputWrapper from '@Component/Input/SquareInputWrapper';

  export let selected = dfns.format(new Date(), 'd.M.yyyy');
  export let update = () => {};

  $: update(selected);
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

<SquareInputWrapper>
  <input
    class="text-center"
    value={selected}
    on:focus={evt => evt.target.click()}
    use:litepicker={{ format: 'D.M.YYYY', onSelect: date => (selected = dfns.format(date, 'd.M.yyyy')), lang: $locale, dropdowns: { minYear: 1970, maxYear: null, months: true, years: true } }} />
</SquareInputWrapper>
