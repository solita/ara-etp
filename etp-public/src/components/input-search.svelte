<script>
  import { onMount } from 'svelte';

  export let label;
  export let name;
  export let validation = () => true;
  export let value;

  let id;
  let valid = true;

  export const validate = () => {
    valid = validation(value);
  };

  onMount(() => (id = Math.random().toString(36).substr(2, 9)));
</script>

<style>
  input {
    background-color: transparent;
  }

  input::placeholder {
    @apply text-black;
  }
</style>

<!-- purgecss: invalid -->

<label for={id} class="sr-only">{label}</label>
<div
  class="w-full relative inline-block bg-white rounded-full border-2 px-4 py-2
  border-green hover:bg-lightgrey"
  class:border-red={!valid}>
  <input
    {id}
    {name}
    value={value ?? ''}
    placeholder={label}
    class="w-full focus:outline-none"
    on:change />
</div>
