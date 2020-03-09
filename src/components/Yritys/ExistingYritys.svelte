<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';

  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import YritysForm from '@Component/Yritys/YritysForm';
  import * as YritysUtils from '@Component/Yritys/yritys-utils';
  import { breadcrumbStore } from '@/stores';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  export let params;
  let id = params.id;
  let yritys = Maybe.None();
  let api = Maybe.None();

  let overlay = false;

  let toggleOverlay = value => () => (overlay = value);

  R.compose(
    Future.fork(
      console.error,
      fetchedYritys => (yritys = Maybe.Some(fetchedYritys))
    ),
    YritysUtils.getYritysByIdFuture(fetch)
  )(id);

  const submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(() =>
          flashMessageStore.add(
            'Yritys',
            'error',
            $_('yritys.messages.save-error')
          )
        ),
        R.tap(toggleOverlay(false))
      ),
      R.compose(
        R.tap(() =>
          flashMessageStore.add(
            'Yritys',
            'success',
            $_('yritys.messages.save-success')
          )
        ),
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(Future.after(500, true)),
    YritysUtils.putYritysByIdFuture(fetch, id),
    R.tap(toggleOverlay(true))
  );

  $: links = [
    {
      text: Maybe.fold('...', R.prop('nimi'), yritys)
    },
    { text: $_('yritys.laatijat') }
  ];

  $: breadcrumbStore.set([
    {
      label: $_('yritys.yritykset'),
      url: '/#/yritykset'
    },
    {
      label: Maybe.fold('...', R.prop('nimi'), yritys),
      url: location.href
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
  {#if yritys.isNone()}
    Loading...
  {:else}
    <div class="w-full">
      <NavigationTabBar {links} />
    </div>
    <div class="w-full min-h-3em">
      <FlashMessage module={'Yritys'} />
    </div>
    <Overlay {overlay}>
      <div slot="content">
        <YritysForm
          {submit}
          existing={true}
          yritys={Maybe.getOrElse(null, yritys)} />
      </div>
      <div slot="overlay-content">
        <Spinner />
      </div>
    </Overlay>
  {/if}
</section>
