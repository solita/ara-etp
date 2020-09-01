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
  import * as kayttajaApi from "@Component/Kayttaja/kayttaja-api";

  import { flashMessageStore } from '@/stores';

  export let params;

  let overlay = false;

  const toggleOverlay = value => { overlay = value };

  let energiatodistus = R.equals(params.version, '2018')
    ? empty.energiatodistus2018()
    : empty.energiatodistus2013();

  let luokittelut = Maybe.None();
  let whoami = Maybe.None();
  let validation = Maybe.None();

  const submit = R.compose(
    Future.fork(
      () => {
        toggleOverlay(false);
        flashMessageStore.add('Energiatodistus', 'error',
            $_('energiatodistus.messages.save-error'));
      },
      ({id}) => {
        flashMessageStore.addPersist('Energiatodistus', 'success',
            $_('energiatodistus.messages.save-success'));
        replace(`/energiatodistus/${params.version}/${id}`);
      }
    ),
    Future.delay(500),
    api.postEnergiatodistus(fetch, params.version),
    R.tap(() => toggleOverlay(true))
  );

  $: title = `Energiatodistus ${params.version} - Uusi luonnos`;

  // Load classifications
  $: R.compose(
    Future.fork(
      () => {
        toggleOverlay(false);
        flashMessageStore.add('Energiatodistus', 'error',
            $_('energiatodistus.messages.load-error'));
      },
      response => {
        luokittelut = Maybe.Some(response[0]);
        validation = Maybe.Some(response[1]);
        whoami = Maybe.Some(response[2]);
        toggleOverlay(false);
      },
    ),
    Future.parallel(3),
    R.append(kayttajaApi.whoami),
    R.juxt([api.luokittelut, api.validation])
  )(params.version);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if luokittelut.isSome()}
      <EnergiatodistusForm
        version={params.version}
        {title}
        {energiatodistus}
        whoami={whoami.some()}
        luokittelut = {luokittelut.some()}
        validation = {validation.some()}
        {submit} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
