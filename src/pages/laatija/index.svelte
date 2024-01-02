<script>
  import Router, { wrap } from 'svelte-spa-router';

  import LaatijaUpload from '@Pages/laatija/upload/laatija-upload';
  import Yritykset from '@Pages/laatija/yritykset';
  import Laatijat from './laatijat';
  import NewLaatija from './new-laatija';
  import NotFound from '@Pages/not-found/not-found';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { announcementsForModule } from '@Utility/announce';

  const prefix = '/laatija';
  const routes = {
    '/all': Laatijat,
    '/new': NewLaatija,
    '/laatijoidentuonti': LaatijaUpload,
    '/:id/yritykset': Yritykset,
    '*': NotFound
  };
  const { clearAnnouncements } = announcementsForModule('Laatija');
</script>

<svelte:window on:hashchange={clearAnnouncements} />

<Router {routes} {prefix} />

<FlashMessage module={'Laatija'} />
