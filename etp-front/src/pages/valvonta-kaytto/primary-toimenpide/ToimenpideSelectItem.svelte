<script>
  import { getContext } from 'svelte';

  export let state;

  // The object received from the context is a store which contains the value we
  // need to check
  let primaryOptionStore = getContext('primaryOption');
</script>

<style type="text/postcss">
  .primary-option {
    @apply font-bold;
  }
</style>

<div>
  {#if $primaryOptionStore === state}
    <strong class="primary-option">{state}</strong>
  {:else}
    {state}
  {/if}
</div>
