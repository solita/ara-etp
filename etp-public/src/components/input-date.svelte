<script>
  import { onMount } from 'svelte';

  import * as parsers from '@/utilities/parsers';
  import * as formats from '@/utilities/formats';

  export let label;
  export let placeholder;
  export let value;
  export let name;
  export let min;
  export let max;
  export let invalidMessage;

  export let model = {};

  $: value = parsers.parseDate(model[name]);

  export let validation = () => true;

  let valid = true;

  export const validate = () => {
    valid = validation(value);
  };

  let id;
  let focused;

  onMount(() => (id = Math.random().toString(36).substr(2, 9)));
</script>

<style>
  input {
    background-color: white;
  }

  input::placeholder {
    @apply text-darkgrey italic;
  }

  .input-parent:focus-within {
    @apply bg-grey border-green;
  }

  .errortext {
    @apply text-xs absolute;
    bottom: -0.4em;
    left: 0;
  }
</style>

<!-- purgecss: 
border-darkgrey
border-green
border-red
-->
<div class="relative w-full flex flex-col pb-8">
  <label for={id} class="sr-only">{label}</label>
  <div
    class:border-red={!valid && value}
    class="input-parent w-full relative inline-block border-b-2 px-4 py-2
    border-darkgrey hover:bg-grey">
    <input
      on:change
      on:focus={_ => (focused = true)}
      on:blur={_ => (focused = false)}
      {id}
      {name}
      {min}
      {max}
      value={formats.formatDateISO(value)}
      {placeholder}
      type="date"
      class="w-full focus:outline-none" />
  </div>

  {#if !focused && invalidMessage && !valid}
    <span class="errortext">{invalidMessage}</span>
  {/if}
</div>
