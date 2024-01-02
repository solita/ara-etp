<script>
  import { onMount } from 'svelte';

  export let label;
  export let value;
  export let format = a => a;
  export let options = [];
  export let name;
  export let disabled;

  let id;

  onMount(() => (id = Math.random().toString(36).substr(2, 9)));
</script>

<style>
  .input-parent:focus-within {
    @apply bg-grey border-green;
  }

  select {
    background-color: transparent;
  }
</style>

<!-- purgecss: 
border-darkgrey
border-green
-->
<div class="relative w-full flex flex-col pb-8">
  <label for={id} class="sr-only">{label}</label>
  <div
    class="input-parent w-full relative inline-block border-b-2 {disabled
      ? 'cursor-not-allowed text-darkgrey border-grey'
      : 'hover:bg-grey border-darkgrey'}">
    <select
      class="w-full px-2 py-2"
      class:cursor-not-allowed={disabled}
      {id}
      {name}
      {disabled}
      bind:value
      on:blur|stopPropagation={evt =>
        evt.target.dispatchEvent(new Event('change'))}>
      {#each options as op}
        <option value={op}>{format(op)}</option>
      {/each}
    </select>
  </div>
</div>
