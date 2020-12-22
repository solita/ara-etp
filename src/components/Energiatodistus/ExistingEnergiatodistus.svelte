<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import EnergiatodistusForm from './EnergiatodistusForm';

  import * as et from './energiatodistus-utils';
  import * as api from './energiatodistus-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import { flashMessageStore } from '@/stores';

  export let params;

  let energiatodistus = Maybe.None();
  let luokittelut = Maybe.None();
  let whoami = Maybe.None();
  let validation = Maybe.None();

  let overlay = true;

  const toggleOverlay = value => {
    overlay = value;
  };

  const resetView = () => {
    overlay = true;
    energiatodistus = Maybe.None();
  };

  $: params.id && resetView();

  let showMissingProperties;

  const submit = (energiatodistus, onSuccessfulSave) =>
    R.compose(
      Future.fork(
        response => {
          toggleOverlay(false);
          if (R.pathEq(['body', 'type'], 'missing-value', response)) {
            showMissingProperties(response.body.missing);
          } else {
            flashMessageStore.add('Energiatodistus', 'error',
              $_(Maybe.orSome('energiatodistus.messages.save-error',
                Response.localizationKey(response))));
          }
        },
        () => {
          toggleOverlay(false);
          flashMessageStore.add(
            'Energiatodistus',
            'success',
            $_('energiatodistus.messages.save-success')
          );
          onSuccessfulSave();
        }
      ),
      Future.delay(500),
      api.putEnergiatodistusById(fetch, params.version, params.id),
      R.tap(() => toggleOverlay(true))
    )(energiatodistus);

  // load energiatodistus and classifications in parallel
  $: R.compose(
    Future.fork(
      response => {
        toggleOverlay(false);
        const msg = Response.notFound(response) ?
          $_('energiatodistus.messages.not-found'):
          $_(Maybe.orSome('energiatodistus.messages.load-error',
            Response.localizationKey(response)));

        flashMessageStore.add('Energiatodistus', 'error', msg);
      },
      response => {
        energiatodistus = Maybe.Some(response[0]);
        luokittelut = Maybe.Some(response[1]);
        whoami = Maybe.Some(response[2]);
        validation = Maybe.Some(response[3]);
        toggleOverlay(false);
      }
    ),
    Future.parallel(5),
    R.prepend(R.__, [
      api.luokittelut(params.version),
      kayttajaApi.whoami,
      api.validation(params.version)
    ]),
    R.tap(() => toggleOverlay(true)),
    api.getEnergiatodistusById(fetch)
  )(params.version, params.id);

  const tilaLabel = R.compose(
    Maybe.orSome($_('energiatodistus.tila.loading')),
    R.map(e => $_('energiatodistus.tila.' + et.tilaKey(e['tila-id'])))
  );

  $: title = `${$_('energiatodistus.title')} ${params.version}/${
    params.id
  } - ${tilaLabel(energiatodistus)}`;
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if energiatodistus.isSome()}
      <EnergiatodistusForm
        version={params.version}
        energiatodistus={energiatodistus.some()}
        luokittelut={luokittelut.some()}
        whoami={whoami.some()}
        validation={validation.some()}
        bind:showMissingProperties
        {submit}
        {title} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
