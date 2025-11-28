<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as versionApi from '@Component/Version/version-api';
  import { isEtp2026Enabled } from '@Utility/config_utils.js';
  import * as Schema from '@Pages/energiatodistus/schema';

  import EnergiatodistusForm from '@Pages/energiatodistus/EnergiatodistusForm';
  import PPPForm from '@Pages/energiatodistus/ppp-form.svelte';
  import PPPSection from '@Pages/energiatodistus/PPPSection.svelte';

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
  let perusparannuspassi = Maybe.None();
  let showPPP = false;

  // Add PPP - creates a new perusparannuspassi via API
  const addPerusparannuspassi = energiatodistusId => () => {
    if (!showPPP && Maybe.isNone(perusparannuspassi)) {
      toggleOverlay(true);
      Future.fork(
        _response => {
          toggleOverlay(false);
          announceError(i18n('energiatodistus.messages.add-ppp-error'));
        },
        result => {
          perusparannuspassi = result;
          showPPP = true;
          toggleOverlay(false);
          announceSuccess(i18n('energiatodistus.messages.add-ppp-success'));
        },
        pppApi.addPerusparannuspassi(fetch, energiatodistusId)
      );
    } else {
      // Just toggle visibility if PPP already exists
      showPPP = !showPPP;
    }
  };

  const submit = (energiatodistus, onSuccessfulSave) => {
    toggleOverlay(true);

    const saveFuture =
      perusparannuspassi && perusparannuspassi.id
        ? Future.parallelObject(2, {
            energiatodistus: api.putEnergiatodistusById(
              fetch,
              params.version,
              params.id
            )(energiatodistus),
            perusparannuspassi: pppApi.putPerusparannuspassi(
              fetch,
              perusparannuspassi.id,
              perusparannuspassi
            )
          })
        : R.map(
            energiatodistus => ({ energiatodistus }),
            api.putEnergiatodistusById(
              fetch,
              params.version,
              params.id
            )(energiatodistus)
          );

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
      },
      R.chain(Future.after(400), saveFuture)
    );
  };

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
        }

        toggleOverlay(false);
      },
      R.chain(
        response =>
          R.map(
            R.mergeLeft(response),
            Future.parallelObject(2, {
              laskutusosoitteet: laatijaApi.laskutusosoitteet(
                Maybe.get(response.energiatodistus['laatija-id'])
              ),
              perusparannuspassi:
                response.energiatodistus['perusparannuspassi-id'] &&
                Maybe.isSome(response.energiatodistus['perusparannuspassi-id'])
                  ? pppApi.getPerusparannuspassi(
                      fetch,
                      Maybe.get(
                        response.energiatodistus['perusparannuspassi-id']
                      )
                    )
                  : Future.resolve(null)
            })
          ),
        Future.parallelObject(7, {
          energiatodistus: api.getEnergiatodistusById(
            params.version,
            params.id
          ),
          luokittelut: api.luokittelutForVersion(params.version),
          whoami: kayttajaApi.whoami,
          validation: api.validation(params.version),
          pppvalidation: pppApi.pppValidation(params.version),
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

  let energiatodistusFormComponent;
  const setFormDirty = () => {
    if (energiatodistusFormComponent) {
      energiatodistusFormComponent.$set({ dirty: true });
    }
  };
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as { energiatodistus, luokittelut, whoami, validation, valvonta, verkkolaskuoperaattorit, laskutusosoitteet, pppvalidation }}
      <EnergiatodistusForm
        bind:this={energiatodistusFormComponent}
        version={params.version}
        {energiatodistus}
        {perusparannuspassi}
        {luokittelut}
        {whoami}
        {validation}
        {valvonta}
        {verkkolaskuoperaattorit}
        {laskutusosoitteet}
        {pppvalidation}
        bind:showMissingProperties
        {submit}
        title={title(energiatodistus)}>
        <!-- PPP section as slot content -->
        {#if isEtp2026Enabled(config) && params.version == 2026}
          <PPPSection
            {showPPP}
            onAddPPP={addPerusparannuspassi(energiatodistus.id)}>
            {#if perusparannuspassi}
              <div on:input={setFormDirty} on:change={setFormDirty}>
                <PPPForm
                  {energiatodistus}
                  inputLanguage={'fi'}
                  {luokittelut}
                  {pppvalidation}
                  bind:perusparannuspassi
                  schema={Schema.perusparannuspassi} />
              </div>
            {/if}
          </PPPSection>
        {/if}
      </EnergiatodistusForm>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
