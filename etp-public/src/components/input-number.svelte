<script>
  import { onMount } from 'svelte';

  export let label;
  export let placeholder;
  export let value;
  export let name;
  export let min;
  export let max;
  export let step;
  export let invalidMessage;

  export let model = {};

  $: value = model[name];

  export let validation = () => true;

  let valid = true;

  export const validate = () => {
    valid = validation(value);
  };

  let id;
  let focused = false;

  onMount(() => (id = Math.random().toString(36).substr(2, 9)));
</script>

<style>
  input {
    background-color: transparent;
  }

  .input-parent:focus-within {
    @apply bg-grey border-green;
  }
  .errortext {
    @apply text-xs absolute;
    bottom: -0.2em;
    left: 0;
  }
</style>

<!-- purgecss: 
border-red
-->
<div class="relative w-full flex flex-col pb-8">
  <label for={id} class="sr-only">{label}</label>
  <div
    class:border-red={!valid && value}
    class="input-parent w-full relative inline-block border-b-2 px-4 py-2 border-darkgrey text-center
   hover:bg-grey">
    <input
      {id}
      {name}
      {min}
      {max}
      {step}
      {value}
      {placeholder}
      on:change
      type="number"
      class="w-full focus:outline-none"
      on:focus={() => (focused = true)}
      on:blur={() => (focused = false)} />
  </div>
  {#if !focused && invalidMessage && !valid && value}
    <span class="errortext">{invalidMessage}</span>
  {/if}
</div>
