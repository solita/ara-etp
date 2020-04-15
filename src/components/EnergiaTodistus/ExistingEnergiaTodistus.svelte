<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import EnergiaTodistusForm from './EnergiaTodistusForm';

  import * as et from './energiatodistus-utils';
  import * as api from './energiatodistus-api';
  import { breadcrumbStore } from '@/stores';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import { flashMessageStore } from '@/stores';

  export let params;

  let energiatodistus = Maybe.None();

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
        flashMessageStore.add('EnergiaTodistus', 'error'),
        R.always($_('energiatodistus.messages.save-error'))
      ),
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('EnergiaTodistus', 'success'),
        R.always($_('energiatodistus.messages.save-success'))
      )
    ),
    Future.both(Future.after(500, true)),
    api.putEnergiatodistusById(fetch, params.version, params.id),
    R.tap(toggleOverlay(true))
  );

  $: R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.compose(
          R.tap(toggleDisabled(true)),
          flashMessageStore.add('EnergiaTodistus', 'error'),
          R.always($_('energiatodistus.messages.load-error'))
        ),
        R.tap(toggleOverlay(false))
      ),
      R.compose(
        response => {
          energiatodistus = Maybe.Some(response[0])
        },
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(Future.after(400, true)),
    Future.parallel(5),
    R.prepend(R.__, [api.kielisyys, api.laatimisvaiheet,
      api.alakayttotarkoitusluokat2018, api.kayttotarkoitusluokat2018]),
    api.getEnergiatodistusById(fetch)
  )(params.version, params.id);

  $: breadcrumbStore.set([et.breadcrumb1stLevel($_),
    {
      label: Maybe.fold('...', R.prop('id'), energiatodistus),
      url: location.href
    }
  ]);

  $: title = `Energiatodistus ${params.version}/${params.id}`;
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if Maybe.isSome(energiatodistus)}
      <EnergiaTodistusForm version={params.version} {disabled}
                           energiatodistus={energiatodistus.some()}
                           {submit} {title}/>
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
