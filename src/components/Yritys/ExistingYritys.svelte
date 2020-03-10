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

  let yritys = Maybe.None();
  let api = Maybe.None();

  let overlay = true;
  let disabled = false;

  const toggleOverlay = value => () => (overlay = value);
  const toggleDisabled = value => () => (disabled = value);

  const resetView = () => {
    overlay = true;
    yritys = Maybe.None();
    disabled = false;
    flashMessageStore.flush();
  };

  $: params.id && resetView();

  $: R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(toggleDisabled(true)),
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Yritys', 'error'),
        R.always($_('yritys.messages.load-error'))
      ),
      R.compose(
        fetchedYritys => (yritys = Maybe.Some(fetchedYritys)),
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(Future.after(400, true)),
    YritysUtils.getYritysByIdFuture(fetch)
  )(params.id);

  $: submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        flashMessageStore.add('Yritys', 'error'),
        R.always($_('yritys.messages.save-error')),
        R.tap(toggleOverlay(false))
      ),
      R.compose(
        flashMessageStore.add('Yritys', 'success'),
        R.always($_('yritys.messages.save-success')),
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(Future.after(500, true)),
    YritysUtils.putYritysByIdFuture(fetch, params.id),
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
        {disabled}
        existing={true}
        yritys={Maybe.getOrElse(YritysUtils.emptyYritys(), yritys)} />
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
</section>
