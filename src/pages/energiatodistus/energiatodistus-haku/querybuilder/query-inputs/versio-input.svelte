<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  export let values = [];
  export let nameprefix;
  export let index = 0;
  export let value = R.head(values);

  const versiot = [2018, 2013];

  let input;

  $: {
    input && (input.value = value);
    input && input.dispatchEvent(new Event('change', { bubbles: true }));
  }
</script>

<style type="text/postcss">
  span::after {
    @apply select-none text-3xl;
    content: 'radio_button_unchecked';
  }

  span.checked::after {
    @apply text-primary;
    content: 'radio_button_checked';
  }

  label {
    @apply mr-4;
  }
</style>

<!-- purgecss: checked -->

<input
  bind:this={input}
  class="sr-only"
  name={`${nameprefix}_value_${index}`} />

<div class="flex flex-row">
  <label>
    <input class="sr-only" type="radio" bind:group={value} value={''} />
    <div class="flex items-center">
      <span
        class="material-icons"
        class:checked={R.not(R.includes(value, versiot))} />
      {$_('energiatodistus.haku.kaikki')}
    </div>
  </label>
  {#each versiot as versio}
    <label>
      <input class="sr-only" type="radio" bind:group={value} value={versio} />
      <div class="flex items-center">
        <span class="material-icons" class:checked={value === versio} />
        {versio}
      </div>
    </label>
  {/each}
</div>
