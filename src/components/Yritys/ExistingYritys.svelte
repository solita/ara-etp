<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import YritysForm from '@Component/Yritys/YritysForm';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import { flashMessageStore, idTranslateStore } from '@/stores';

  import * as api from './yritys-api';
  import * as Locales from '@Language/locale-utils';


  export let params;

  let yritys = Maybe.None();

  let overlay = true;
  let disabled = false;

  let luokittelut = Maybe.None();

  const toggleOverlay = value => { overlay = value };
  const toggleDisabled = value => { disabled = value };

  const resetView = () => {
    overlay = true;
    yritys = Maybe.None();
    disabled = false;
  };

  $: params.id && resetView();

  $: submit = updatedYritys =>
    R.compose(
      Future.fork(
        response => {
          toggleOverlay(false);
          flashMessageStore.add(
            'Yritys',
            'error',
            Locales.uniqueViolationMessage($_, response, 'yritys.messages.save-error'))
        },
        () => {
          toggleOverlay(false);
          flashMessageStore.add('Yritys', 'success', $_('yritys.messages.save-success'));
          idTranslateStore.updateYritys(updatedYritys);
        }
      ),
      Future.delay(400),
      api.putYritysById(fetch, params.id),
      R.tap(() => toggleOverlay(true))
    )(updatedYritys);

  // Load yritys and all luokittelut used in yritys form
  $: R.compose(
    Future.fork(
      () => {
        toggleOverlay(false);
        flashMessageStore.add('Yritys', 'error', $_('yritys.messages.load-error'));
      },
      response => {
        luokittelut = Maybe.Some(response[0]);
        yritys = Maybe.Some(response[1]);
        toggleOverlay(false);
      }
    ),
    Future.delay(400),
    Future.both(api.luokittelut),
    api.getYritysById(fetch)
  )(params.id);

</script>

<Overlay {overlay}>
  <div slot="content">
    {#if luokittelut.isSome()}
      <YritysForm
        {submit}
        {disabled}
        luokittelut={luokittelut.some()}
        existing={false}
        yritys={yritys.some()} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
