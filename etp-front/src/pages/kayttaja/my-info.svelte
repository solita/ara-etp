<script>
  import { replace } from 'svelte-spa-router';

  export let whoami;
  replace(`/kayttaja/${whoami.id}`);
</script>
