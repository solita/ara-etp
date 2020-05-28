<script>
  import * as R from 'ramda';
  import { replace } from 'svelte-spa-router';
  import { currentUserStore } from '@/stores';

  import * as Maybe from '@Utility/maybe-utils';

  R.forEach(kayttaja => replace(`/kayttaja/${kayttaja.id}`), $currentUserStore);
</script>
