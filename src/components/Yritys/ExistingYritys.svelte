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

  let overlay = true;
  let disabled = false;

  const toggleOverlay = value => () => (overlay = value);
  const toggleDisabled = value => () => (disabled = value);

  const resetView = () => {
    overlay = true;
    yritys = Maybe.None();
    disabled = false;
  };

  $: params.id && resetView();

  $: submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Yritys', 'error'),
        R.always($_('yritys.messages.save-error'))
      ),
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Yritys', 'success'),
        R.always($_('yritys.messages.save-success'))
      )
    ),
    Future.both(Future.after(500, true)),
    YritysUtils.putYritysByIdFuture(fetch, params.id),
    R.tap(toggleOverlay(true))
  );

  $: R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.compose(
          R.tap(toggleDisabled(true)),
          flashMessageStore.add('Yritys', 'error'),
          R.always($_('yritys.messages.load-error'))
        ),
        R.tap(toggleOverlay(false))
      ),
      R.compose(
        fetchedYritys => (yritys = Maybe.Some(fetchedYritys)),
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(Future.after(400, true)),
    YritysUtils.getYritysByIdFuture(fetch)
  )(params.id);

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

<Overlay {overlay}>
  <div slot="content">
    <YritysForm
      {submit}
      {disabled}
      existing={Maybe.isSome(yritys)}
      yritys={Maybe.getOrElse(YritysUtils.emptyYritys(), yritys)} />
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
