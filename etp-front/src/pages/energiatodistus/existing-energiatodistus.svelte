<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as versionApi from '@Component/Version/version-api';
  import { isEtp2026Enabled } from '@Utility/config_utils.js';
  import * as Schema from '@Pages/energiatodistus/schema';
  import * as Empty from '@Pages/energiatodistus/empty';

  import EnergiatodistusForm from '@Pages/energiatodistus/EnergiatodistusForm';
  import PPPForm from '@Pages/energiatodistus/ppp-form.svelte';
  import PPPWrapper from '@Pages/energiatodistus/PPPWrapper.svelte';

  import * as et from '@Pages/energiatodistus/energiatodistus-utils';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as pppApi from '@Pages/energiatodistus/perusparannuspassi-api';
  import * as ValvontaApi from '@Pages/valvonta-oikeellisuus/valvonta-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as laskutusApi from '@Utility/api/laskutus-api';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import { announcementsForModule } from '@Utility/announce';

  export let params;

  const i18n = $_;
  const i18nRoot = 'energiatodistus';
  const { announceError, announceSuccess } =
    announcementsForModule('Energiatodistus');
  let resources = Maybe.None();

  let overlay = true;

  const toggleOverlay = value => {
    overlay = value;
  };

  let showMissingProperties;

  let config = {};
  Future.fork(
    _ => {
      config = {};
    },
    loadedConfig => {
      config = loadedConfig;
    },
    versionApi.getConfig
  );

  // PPP state
  let perusparannuspassi = null;
  let showPPP = false;

  // Toggle PPP form visibility
  const togglePPP = (energiatodistusId, energiatodistus) => () => {
    if (!showPPP && !perusparannuspassi) {
      // Create empty PPP when showing for the first time
      perusparannuspassi = Empty.perusparannuspassi(energiatodistusId);
    }
    showPPP = !showPPP;
  };

  const submit = (energiatodistus, onSuccessfulSave) =>
    R.compose(
      Future.fork(
        response => {
          toggleOverlay(false);
          if (R.pathEq('missing-value', ['body', 'type'], response)) {
            showMissingProperties(response.body.missing);
          } else {
            announceError(i18n(Response.errorKey(i18nRoot, 'save', response)));
          }
        },
        () => {
          toggleOverlay(false);
          announceSuccess($_('energiatodistus.messages.save-success'));
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
    perusparannuspassi = null;
    showPPP = false;

    Future.fork(
      response => {
        toggleOverlay(false);
        announceError(i18n(Response.errorKey404(i18nRoot, 'load', response)));
      },
      response => {
        resources = Maybe.Some(response);

        // Set PPP if it exists
        if (response.perusparannuspassi) {
          perusparannuspassi = response.perusparannuspassi;
          showPPP = true;
          console.log(perusparannuspassi);
        }

        toggleOverlay(false);
      },
      R.chain(
        response =>
          R.chain(
            responseWithLaskutus => {
              const pppId = response.energiatodistus['perusparannuspassi-id'];
              return Future.parallelObject(1, {
                perusparannuspassi:
                  pppId && Maybe.isSome(pppId)
                    ? pppApi.getPerusparannuspassi(fetch, Maybe.get(pppId))
                    : Future.resolve(null)
              }).pipe(
                R.map(pppResponse =>
                  R.assoc(
                    'perusparannuspassi',
                    pppResponse.perusparannuspassi,
                    responseWithLaskutus
                  )
                )
              );
            },
            R.map(
              R.assoc('laskutusosoitteet', R.__, response),
              laatijaApi.laskutusosoitteet(
                Maybe.get(response.energiatodistus['laatija-id'])
              )
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
        title={title(energiatodistus)}>
        <!-- PPP section as slot content -->
        {#if isEtp2026Enabled(config) && params.version == 2026}
          <PPPWrapper
            {showPPP}
            onAddPPP={togglePPP(energiatodistus.id, energiatodistus)}>
            {#if perusparannuspassi}
              <PPPForm
                {energiatodistus}
                inputLanguage={'fi'}
                {luokittelut}
                bind:perusparannuspassi
                schema={Schema.perusparannuspassi} />
            {/if}
          </PPPWrapper>
        {/if}
      </EnergiatodistusForm>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
