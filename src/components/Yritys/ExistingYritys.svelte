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
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore, idTranslateStore } from '@/stores';

  import * as laskutusApi from '@Component/Laskutus/laskutus-api';


  export let params;

  let yritys = Maybe.None();

  let overlay = true;
  let disabled = false;

  let luokittelut = Maybe.None();

  const toggleOverlay = value => () => (overlay = value);
  const toggleDisabled = value => () => (disabled = value);

  const resetView = () => {
    overlay = true;
    yritys = Maybe.None();
    disabled = false;
  };

  $: params.id && resetView();

  $: submit = updatedYritys =>
    R.compose(
      Future.forkBothDiscardFirst(
        R.compose(
          R.tap(toggleOverlay(false)),
          flashMessageStore.add('Yritys', 'error'),
          R.always($_('yritys.messages.save-error'))
        ),
        R.compose(
          R.tap(toggleOverlay(false)),
          flashMessageStore.add('Yritys', 'success'),
          R.always($_('yritys.messages.save-success')),
          R.tap(() => idTranslateStore.updateYritys(updatedYritys))
        )
      ),
      Future.both(Future.after(500, true)),
      YritysUtils.putYritysByIdFuture(fetch, params.id),
      R.tap(toggleOverlay(true))
    )(updatedYritys);

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
        R.tap(idTranslateStore.updateYritys),
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(Future.after(400, true)),
    YritysUtils.getYritysByIdFuture(fetch)
  )(params.id);

  // Load classifications
  $: R.compose(
    Future.fork(
      R.compose(
        R.tap(toggleOverlay(false)),
        R.tap(flashMessageStore.add('Yritys', 'error')),
        R.always($_('yritys.messages.load-error'))
      ),
      R.compose(
        response => {
          luokittelut = Maybe.Some(response);
        },
        R.tap(toggleOverlay(false))
      )
    ),
    laskutusApi.luokittelut
  )();

</script>

<Overlay {overlay}>
  <div slot="content">
    {#if luokittelut.isSome()}
      <YritysForm
        {submit}
        {disabled}
        luokittelut={luokittelut.some()}
        existing={Maybe.isSome(yritys)}
        yritys={Maybe.orSome(YritysUtils.emptyYritys(), yritys)} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
