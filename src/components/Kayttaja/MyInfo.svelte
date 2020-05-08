<script>
  import * as R from 'ramda';
  import { replace } from 'svelte-spa-router';
  import { currentUserStore } from '@/stores';

  import * as Maybe from '@Utility/maybe-utils';

  const routeForKayttaja = kayttaja => {
    if (kayttaja.rooli === 0) {
      replace(`/kayttaja/${kayttaja.id}`);
    } else {
      console.log('Roolia ei tuettu');
    }
  };

  R.forEach(routeForKayttaja, $currentUserStore);
</script>

{#if R.compose( Maybe.isSome, R.filter(R.compose( R.equals(1), R.prop('rooli') )) )($currentUserStore)}
  Pätevyydentoteajan tiedot tulevat tänne.
{/if}
