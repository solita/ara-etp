<script>
  import { setContext } from 'svelte';

  export let primaryOption = '';
  setContext('primaryOption', primaryOption);
</script>

<div>
  <slot />
</div>
