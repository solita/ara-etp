<script>
  import * as R from 'ramda';
  import { replace } from 'svelte-spa-router';
  import { currentUserStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import * as Navigation from '@Utility/navigation';

  const landingPageForKayttaja = R.compose(
    R.prop('href'),
    R.head,
    Navigation.parseRoot($_)
  );

  R.forEach(
    R.compose(
      replace,
      landingPageForKayttaja
    ),
    $currentUserStore
  );
</script>
