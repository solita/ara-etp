<script>
  import { replace } from '@Component/Router/router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import EnergiatodistusForm from './EnergiatodistusForm';
  import * as et from './energiatodistus-utils';
  import * as empty from './empty';
  import * as api from './energiatodistus-api';

  import { flashMessageStore } from '@/stores';

  export let params;

  let overlay = false;

  const toggleOverlay = value => () => (overlay = value);

  let energiatodistus = R.equals(params.version, '2018')
    ? empty.energiatodistus2018()
    : empty.energiatodistus2013();

  let luokittelut = Maybe.None();

  const submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Energiatodistus', 'error'),
        R.always($_('energiatodistus.messages.save-error'))
      ),
      R.compose(
        R.tap(() =>
          flashMessageStore.addPersist(
            'Energiatodistus',
            'success',
            $_('energiatodistus.messages.save-success')
          )
        ),
        ({ id }) => replace(`/energiatodistus/${params.version}/${id}`)
      )
    ),
    Future.both(Future.after(500, true)),
    api.postEnergiatodistus(fetch, params.version),
    R.tap(toggleOverlay(true))
  );

  $: title = `Energiatodistus ${params.version} - Uusi luonnos`;

  // Load classifications
  $: R.compose(
    Future.fork(
      R.compose(
        R.tap(toggleOverlay(false)),
        R.tap(flashMessageStore.add('Energiatodistus', 'error')),
        R.always($_('energiatodistus.messages.load-error'))
      ),
      R.compose(
        response => {
          luokittelut = Maybe.Some(response);
        },
        R.tap(toggleOverlay(false))
      )
    ),
    api.luokittelut
  )(params.version);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if luokittelut.isSome()}
      <EnergiatodistusForm
        version={params.version}
        {title}
        {energiatodistus}
        luokittelut = {luokittelut.some()}
        {submit} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
