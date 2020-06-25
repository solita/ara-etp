<script>
  import { replace } from '@Component/Router/router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import YritysForm from '@Component/Yritys/YritysForm';
  import * as YritysUtils from './yritys-utils';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  import * as laskutusApi from '@Component/Laskutus/laskutus-api';


  let overlay = false;

  const toggleOverlay = value => () => (overlay = value);

  let yritys = YritysUtils.emptyYritys();
  let luokittelut = Maybe.None();

  const submit = R.compose(
    Future.fork(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Yritys', 'error'),
        R.ifElse(
          R.equals(409),
          _ => $_('yritys.messages.save-conflict'),
          _ => $_('yritys.messages.save-error')
        )
      ),
      R.compose(
        R.tap(_ =>
          flashMessageStore.addPersist(
            'Yritys',
            'success',
            $_('yritys.messages.save-success')
          )
        ),
        ({ id }) => replace(`/yritys/${id}`)
      )
    ),
    YritysUtils.postYritysFuture(fetch),
    R.tap(toggleOverlay(true))
  );

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
      <YritysForm {yritys} luokittelut={luokittelut.some()} {submit} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
