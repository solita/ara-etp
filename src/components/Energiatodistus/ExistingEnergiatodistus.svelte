<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import EnergiatodistusForm from './EnergiatodistusForm';

  import * as et from './energiatodistus-utils';
  import * as api from './energiatodistus-api';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import { flashMessageStore } from '@/stores';

  export let params;

  let energiatodistus = Maybe.None();
  let luokittelut = Maybe.None();

  let overlay = true;
  let disabled = false;

  const toggleOverlay = value => () => (overlay = value);
  const toggleDisabled = value => () => (disabled = value);

  const resetView = () => {
    overlay = true;
    energiatodistus = Maybe.None();
    disabled = false;
  };

  $: params.id && resetView();

  $: submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Energiatodistus', 'error'),
        R.always($_('energiatodistus.messages.save-error'))
      ),
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Energiatodistus', 'success'),
        R.always($_('energiatodistus.messages.save-success'))
      )
    ),
    Future.both(Future.after(500, true)),
    api.putEnergiatodistusById(fetch, params.version, params.id),
    R.tap(toggleOverlay(true))
  );

  $: R.compose(
    Future.fork(
      R.compose(
        R.compose(
          R.tap(toggleDisabled(true)),
          flashMessageStore.add('Energiatodistus', 'error'),
          R.always($_('energiatodistus.messages.load-error'))
        ),
        R.tap(toggleOverlay(false))
      ),
      R.compose(
        response => {
          energiatodistus = Maybe.Some(response[0]);
          luokittelut = Maybe.Some(response[1]);
        },
        R.tap(toggleOverlay(false))
      )
    ),
    Future.parallel(5),
    R.pair(R.__, api.luokittelut(params.version)),
    api.getEnergiatodistusById(fetch)
  )(params.version, params.id);

  const tilaLabel = R.compose(
    Maybe.orSome($_('energiatodistus.tila.loading')),
    R.map(e => $_('energiatodistus.tila.' + et.tilaKey(e['tila-id'])))
  );

  $: title = `${$_('energiatodistus.title')} ${params.version}/${
    params.id
  } - ${tilaLabel(energiatodistus)}`;

  $: disabled = Maybe.exists(
    R.compose(
      Maybe.isSome,
      R.prop('allekirjoitusaika')
    ),
    energiatodistus
  );
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if Maybe.isSome(energiatodistus)}
      <EnergiatodistusForm
        version={params.version}
        {disabled}
        energiatodistus={energiatodistus.some()}
        luokittelut={luokittelut.some()}
        {submit}
        {title} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
