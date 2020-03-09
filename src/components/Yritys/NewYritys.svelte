<script>
  import { tick } from 'svelte';
  import { replaceFlushFlashMessages } from '@Component/Router/router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import YritysForm from '@Component/Yritys/YritysForm';
  import * as YritysUtils from '@Component/Yritys/yritys-utils';
  import { breadcrumbStore } from '@/stores';
  import * as YritysUtils from './yritys-utils';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  let overlay = false;

  const toggleOverlay = value => () => (overlay = value);

  let yritys = YritysUtils.emptyYritys();

  const submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        Future.value(flashMessageStore.add('Yritys', 'error')),
        R.always(Future.after(400, $_('yritys.messages.save-error'))),
        R.tap(toggleOverlay(false))
      ),
      R.compose(
        Future.value(flashMessageStore.add('Yritys', 'success')),
        Future.after(400),
        R.always($_('yritys.messages.save-success')),
        ({ id }) => replaceFlushFlashMessages(`/yritys/${id}`)
      )
    ),
    Future.both(Future.after(500, true)),
    YritysUtils.postYritysFuture(fetch),
    R.tap(toggleOverlay(true))
  );

  $: links = [
    {
      text: $_('yritys.uusi_yritys')
    },
    { text: $_('yritys.laatijat') }
  ];

  breadcrumbStore.set([
    {
      label: $_('yritys.yritykset'),
      url: '/#/yritykset'
    },
    {
      label: $_('yritys.uusi_yritys'),
      url: window.location.href
    }
  ]);
</script>

<style type="text/postcss">
  .content {
    @apply flex flex-col -my-4 pb-8;
  }

  .content h1 :not(first) {
    @apply py-6;
  }
</style>

<section class="content">
  <div class="w-full">
    <NavigationTabBar {links} />
  </div>
  <div class="w-full min-h-3em">
    <FlashMessage module={'Yritys'} />
  </div>
  <Overlay {overlay}>
    <div slot="content">
      <YritysForm {yritys} {submit} />
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
</section>
