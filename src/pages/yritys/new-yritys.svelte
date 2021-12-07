<script>
  import { replace } from '@Component/Router/router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import YritysForm from '@Pages/yritys/yritys-form';
  import * as YritysUtils from '@Pages/yritys/yritys-utils';
  import { flashMessageStore } from '@/stores';

  import * as api from '@Pages/yritys/yritys-api';
  import * as Locales from '@Language/locale-utils';

  let overlay = false;

  const toggleOverlay = value => {
    overlay = value;
  };

  let yritys = YritysUtils.emptyYritys;
  let luokittelut = Maybe.None();

  const clean = _ => {
    yritys = YritysUtils.emptyYritys;
  };

  const submit = R.compose(
    Future.fork(
      response => {
        toggleOverlay(false);
        flashMessageStore.add(
          'yritys',
          'error',
          Locales.uniqueViolationMessage(
            $_,
            response,
            'yritys.messages.save-error'
          )
        );
      },
      ({ id }) => {
        flashMessageStore.addPersist(
          'yritys',
          'success',
          $_('yritys.messages.save-success')
        );

        replace(`/yritys/${id}`);
      }
    ),
    api.postYritys(fetch),
    R.tap(() => toggleOverlay(true))
  );

  // Load classifications
  $: Future.fork(
    () => {
      toggleOverlay(false);
      flashMessageStore.add(
        'yritys',
        'error',
        $_('yritys.messages.load-error')
      );
    },
    response => {
      luokittelut = Maybe.Some(response);
      toggleOverlay(false);
    },
    api.luokittelut
  );
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if luokittelut.isSome()}
      <YritysForm
        bind:yritys
        luokittelut={luokittelut.some()}
        {submit}
        cancel={clean} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
