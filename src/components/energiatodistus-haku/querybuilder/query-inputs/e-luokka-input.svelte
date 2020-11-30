<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  const luokat = ['A', 'B', 'C', 'D', 'E', 'F'];

  export let values = [];
  export let nameprefix;
  export let index = 0;

  let group = R.head(values);
  let input;

  $: {
    input && (input.value = group);
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }
</script>

<style type="text/postcss">
  span::after {
    @apply select-none text-3xl;
    content: 'check_box_outline_blank';
  }

  span.checked::after {
    @apply text-primary;
    content: 'check_box';
  }
</style>

<!-- purgecss: checked -->

<input
  bind:this={input}
  class="sr-only"
  name={`${nameprefix}_value_${index}`} />

<div class="flex flex-row">
  {#each luokat as luokka}
    <label class="mr-4">
      <input
        class="sr-only"
        on:change|stopPropagation
        type="checkbox"
        bind:group
        value={luokka} />
      <div class="flex items-center">
        <span
          class="material-icons"
          class:checked={R.includes(luokka, group)} />
        {luokka}
      </div>
    </label>
  {/each}
</div>
