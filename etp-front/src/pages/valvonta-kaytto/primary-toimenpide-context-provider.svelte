<script>
  import { setContext } from 'svelte';
  import { writable } from 'svelte/store';

  export let primaryOption = '';

  // Store the primaryOption received as a component prop to a store
  // and then put the store to a context.
  // Context can be set only when component is created. Putting the value inside
  // a store allows us to update it later when it changes.
  let primaryOptionStore = writable(primaryOption);
  setContext('primaryOption', primaryOptionStore);

  $: $primaryOptionStore = primaryOption;
</script>

<div>
  <slot />
</div>
