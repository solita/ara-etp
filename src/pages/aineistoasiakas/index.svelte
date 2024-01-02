<script>
  import Router from 'svelte-spa-router';

  import NewAineistoasiakas from './new-aineistoasiakas';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';

  import { announcementsForModule } from '@Utility/announce';

  const prefix = '/aineistoasiakas';
  const routes = {
    '/new': NewAineistoasiakas
  };
  const { clearAnnouncements } = announcementsForModule('aineistoasiakas');
</script>

<svelte:window on:hashchange={clearAnnouncements} />

<Router {routes} {prefix} />
<FlashMessage module={'aineistoasiakas'} />
