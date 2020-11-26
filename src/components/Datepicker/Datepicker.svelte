<script>
  import { locale } from '@Language/i18n';
  import * as formats from '@Utility/formats';

  import SquareInputWrapper from '@Component/Input/SquareInputWrapper';

  import { litepicker } from './litepicker';

  export let value = new Date();

  let input;
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

<div
  use:litepicker={{ lang: $locale, update: val => {
      value = val;
    } }}>
  <SquareInputWrapper>
    <input
      class="text-center"
      value={formats.formatDateInstant(value)}
      bind:this={input}
      placeholder={'dd.mm.yyyy'} />
  </SquareInputWrapper>
</div>
