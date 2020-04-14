<script>
  import * as R from 'ramda';
  import { replace } from 'svelte-spa-router';
  import { currentUserStore } from '@/stores';
  import * as Navigation from '@Utility/navigation';

  const landingPageForKayttaja = R.compose(
    R.prop('href'),
    R.head,
    Navigation.linksForKayttaja
  );

  R.forEach(
    R.compose(
      replace,
      landingPageForKayttaja
    ),
    $currentUserStore
  );
</script>
