<script>
  import { onMount } from 'svelte';

  export let label;
  export let placeholder;
  export let value;
  export let name;

  export let validation = () => true;

  let valid = true;

  export let invalidMessage;

  export const validate = () => {
    valid = validation(value);
  };

  let id;
  let focused;

  onMount(() => (id = Math.random().toString(36).substr(2, 9)));
</script>

<style>
  input {
    background-color: transparent;
  }

  input::placeholder {
    @apply text-darkgrey italic;
  }

  .input-parent:focus-within {
    @apply bg-grey border-green;
  }

  .errortext {
    @apply text-xs absolute;
    bottom: 1em;
    left: 0;
  }
</style>

<!-- purgecss: 
border-red
-->
<div class="relative w-full flex flex-col pb-8">
  <label for={id} class="sr-only">{label}</label>
  <div
    class:border-red={!valid}
    class="input-parent w-full relative inline-block border-b-2 px-4 py-2 border-darkgrey hover:bg-grey">
    <input
      {id}
      {name}
      bind:value
      {placeholder}
      class="w-full focus:outline-none"
      on:focus={() => (focused = true)}
      on:blur={() => (focused = false)}
      on:change />
  </div>
  {#if !focused && invalidMessage && !valid}
    <span class="errortext">{invalidMessage}</span>
  {/if}
</div>
