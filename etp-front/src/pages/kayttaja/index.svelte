<script>
  import Router from 'svelte-spa-router';

  import Kayttajat from './kayttajat';
  import ExistingKayttaja from './existing-kayttaja';
  import NewKayttaja from './new-kayttaja';
  import History from '@Pages/kayttaja/history';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';

  import { announcementsForModule } from '@Utility/announce';

  const { clearAnnouncements } = announcementsForModule('kayttaja');

  const prefix = '/kayttaja';
  const routes = {
    '/all': Kayttajat,
    '/new': NewKayttaja,
    '/:id': ExistingKayttaja,
    '/:id/muutoshistoria': History
  };
</script>

<svelte:window on:hashchange={clearAnnouncements} />

<Router {routes} {prefix} />
<FlashMessage module={'kayttaja'} />
