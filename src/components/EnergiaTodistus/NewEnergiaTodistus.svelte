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
  import EnergiaTodistusForm from './EnergiaTodistusForm';
  import * as et from './energiatodistus-utils';
  import * as api from './energiatodistus-api';

  import { flashMessageStore, breadcrumbStore } from '@/stores';

  export let params;

  let overlay = false;

  const toggleOverlay = value => () => (overlay = value);

  let energiatodistus = R.equals(params.year, '2018') ?
      et.emptyEnergiatodistus2018() :
      et.emptyEnergiatodistus2013();

  const submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('EnergiaTodistus', 'error'),
        R.always($_('energiatodistus.messages.save-error'))
      ),
      R.compose(
        R.tap(() =>
          flashMessageStore.addPersist(
            'EnergiaTodistus',
            'success',
            $_('energiatodistus.messages.save-success')
          )
        ),
        ({ id }) => replace(`/energiatodistus/${params.year}/${id}`)
      )
    ),
    Future.both(Future.after(500, true)),
    api.postEnergiatodistus(fetch, params.year),
    R.tap(toggleOverlay(true))
  );

  breadcrumbStore.set([
    et.breadcrumb1stLevel($_),
    {
      label: $_('energiatodistus.breadcrumb.uusi-energiatodistus') + ' ' + params.year,
      url: window.location.href
    }
  ]);
</script>

<Overlay {overlay}>
  <div slot="content">
    <EnergiaTodistusForm year={params.year} {energiatodistus} {submit}/>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
