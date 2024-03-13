<script>
  import { setContext } from 'svelte';
  import { writable } from 'svelte/store';

  export let primaryOption = '';

  let primaryOptionStore = writable(primaryOption);
  setContext('primaryOption', primaryOptionStore);

  $: $primaryOptionStore = primaryOption;
</script>

<div>
  <slot />
</div>
