<script>
  import { getContext } from 'svelte';

  export let state;
  let isPrimaryOption = getContext('primaryOption');
  console.log('primaryOption', isPrimaryOption);
</script>

<style type="text/postcss">
  .primary-option {
    @apply font-bold;
  }
</style>

<div class:primary-option={isPrimaryOption === state}>{state}</div>
