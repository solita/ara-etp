<script>
  import { locale } from '@Language/i18n';
  import * as formats from '@Utility/formats';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as R from 'ramda';

  import { litepicker } from './litepicker';
  import SimpleInput from '@Component/Input/SimpleInput';
  import SquareInputWrapper from '@Component/Input/SquareInputWrapper';

  export let selected = new Date();
  export let start = new Date();
  export let end = new Date();
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
    on:focus={evt => evt.target.click()}
    use:litepicker={{ lang: $locale, dropdowns: { minYear: 1990, maxYear: null, months: true, years: true } }} />
</SquareInputWrapper>
