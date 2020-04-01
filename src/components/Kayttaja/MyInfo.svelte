<script>
  import * as R from 'ramda';
  import { replace } from 'svelte-spa-router';
  import { currentUserStore } from '@/stores';

  const routeForKayttaja = kayttaja => {
    if (kayttaja.rooli === 0) {
      replace(`/laatija/${kayttaja.id}`);
    }
  };

  R.forEach(routeForKayttaja, $currentUserStore);
</script>
