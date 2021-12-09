<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import EnergiatodistusForm from '@Pages/energiatodistus/EnergiatodistusForm';

  import * as et from '@Pages/energiatodistus/energiatodistus-utils';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as ValvontaApi from '@Pages/valvonta-oikeellisuus/valvonta-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as laskutusApi from '@Utility/api/laskutus-api';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import { flashMessageStore } from '@/stores';
  import * as Inputs from '@Pages/energiatodistus/inputs';

  export let params;

  const i18n = $_;
  const i18nRoot = 'energiatodistus';
  let resources = Maybe.None();

  let overlay = true;

  const toggleOverlay = value => {
    overlay = value;
  };

  let showMissingProperties;

  const submit = (energiatodistus, onSuccessfulSave) =>
    R.compose(
      Future.fork(
        response => {
          toggleOverlay(false);
          if (R.pathEq(['body', 'type'], 'missing-value', response)) {
            showMissingProperties(response.body.missing);
          } else {
            flashMessageStore.add(
              'Energiatodistus',
              'error',
              i18n(Response.errorKey(i18nRoot, 'save', response))
            );
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
      R.chain(Future.after(400)),
      api.putEnergiatodistusById(fetch, params.version, params.id),
      R.tap(() => toggleOverlay(true))
    )(energiatodistus);

  // load energiatodistus and classifications in parallel
  const load = params => {
    toggleOverlay(true);
    // form is recreated in reload - side effect is scroll to up
    resources = Maybe.None();
    Future.fork(
      response => {
        toggleOverlay(false);
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          i18n(Response.errorKey404(i18nRoot, 'load', response))
        );
      },
      response => {
        resources = Maybe.Some(response);
        toggleOverlay(false);
      },
      R.chain(
        response =>
          R.map(
            R.assoc('laskutusosoitteet', R.__, response),
            laatijaApi.laskutusosoitteet(
              Maybe.get(response.energiatodistus['laatija-id'])
            )
          ),
        Future.parallelObject(6, {
          energiatodistus: api.getEnergiatodistusById(
            params.version,
            params.id
          ),
          luokittelut: api.luokittelutForVersion(params.version),
          whoami: kayttajaApi.whoami,
          validation: api.validation(params.version),
          valvonta: ValvontaApi.getValvonta(params.id),
          verkkolaskuoperaattorit: laskutusApi.verkkolaskuoperaattorit
        })
      )
    );
  };
  $: load(params);

  const tilaLabel = energiatodistus =>
    $_('energiatodistus.tila.' + et.tilaKey(energiatodistus['tila-id']));

  const title = energiatodistus =>
    `${$_('energiatodistus.title')} ${params.version}/${
      params.id
    } - ${tilaLabel(energiatodistus)}`;
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as { energiatodistus, luokittelut, whoami, validation, valvonta, verkkolaskuoperaattorit, laskutusosoitteet }}
      <EnergiatodistusForm
        version={params.version}
        {energiatodistus}
        {luokittelut}
        {whoami}
        {validation}
        {valvonta}
        {verkkolaskuoperaattorit}
        {laskutusosoitteet}
        bind:showMissingProperties
        {submit}
        title={title(energiatodistus)} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
