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
  import * as api from './energiatodistus-api';

  import { flashMessageStore, breadcrumbStore } from '@/stores';

  export let params;

  let overlay = false;
  let failure = false;

  const toggleOverlay = value => () => (overlay = value);

  let energiatodistus = R.equals(params.version, '2018')
    ? et.emptyEnergiatodistus2018()
    : et.emptyEnergiatodistus2013();

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

  breadcrumbStore.set([
    et.breadcrumb1stLevel($_),
    {
      label:
        $_('energiatodistus.breadcrumb.uusi-energiatodistus') +
        ' ' +
        params.version,
      url: window.location.href
    }
  ]);

  $: title = `Energiatodistus ${params.version} - Uusi luonnos`;

  // Load classifications to cache
  $: R.compose(
    Future.fork(
      R.compose(
        R.tap(() => {
          failure = true;
        }),
        R.tap(toggleOverlay(false)),
        R.tap(flashMessageStore.add('Energiatodistus', 'error')),
        R.always($_('energiatodistus.messages.load-error'))
      ),
      R.tap(toggleOverlay(false))
    ),
    Future.parallel(5)
  )([
    api.kielisyys,
    api.laatimisvaiheet,
    api.alakayttotarkoitusluokat2018,
    api.kayttotarkoitusluokat2018
  ]);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if !failure}
      <EnergiatodistusForm
        version={params.version}
        {title}
        {energiatodistus}
        {submit} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
