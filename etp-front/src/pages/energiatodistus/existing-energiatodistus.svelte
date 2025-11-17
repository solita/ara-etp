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
  let pppMarkedForDeletion = false; // Track if PPP should be deleted on save

  // Add PPP - creates a new perusparannuspassi via API
  const addPerusparannuspassi = energiatodistusId => () => {
    if (pppMarkedForDeletion) {
      // If PPP is marked for deletion, show a message that user needs to save first
      announceError(i18n('energiatodistus.messages.save-before-adding-ppp'));
      return;
    }

    if (!showPPP && Maybe.isNone(perusparannuspassi)) {
      toggleOverlay(true);
      Future.fork(
        _response => {
          toggleOverlay(false);
          announceError(i18n('energiatodistus.messages.add-ppp-error'));
        },
        result => {
          // After creation, fetch the full PPP to ensure proper deserialization
          Future.fork(
            _error => {
              toggleOverlay(false);
              announceError(i18n('energiatodistus.messages.add-ppp-error'));
            },
            fetchedPpp => {
              perusparannuspassi = fetchedPpp;
              showPPP = true;
              toggleOverlay(false);
              announceSuccess(i18n('energiatodistus.messages.add-ppp-success'));
            },
            pppApi.getPerusparannuspassi(fetch, result.id)
          );
        },
        pppApi.addPerusparannuspassi(fetch, energiatodistusId)
      );
    } else {
      // Just toggle visibility if PPP already exists
      showPPP = !showPPP;
    }
  };

  // Delete PPP - marks for deletion, actual delete happens on save
  const deletePerusparannuspassi = () => {
    if (perusparannuspassi && perusparannuspassi.id) {
      // Mark for deletion (will be deleted when form is saved)
      pppMarkedForDeletion = true;
      showPPP = false;
      setFormDirty(); // Mark form as dirty so save buttons are enabled
      announceSuccess(i18n('energiatodistus.messages.delete-ppp-success'));
    } else {
      // If no ID, just clear local state
      perusparannuspassi = Maybe.None();
      showPPP = false;
    }
  };

  const submit = (energiatodistus, onSuccessfulSave) => {
    toggleOverlay(true);

    // Build the save operation based on PPP state
    let saveFuture;

    if (pppMarkedForDeletion && perusparannuspassi && perusparannuspassi.id) {
      // PPP is marked for deletion - delete it along with saving ET
      saveFuture = Future.parallelObject(2, {
        energiatodistus: api.putEnergiatodistusById(
          fetch,
          params.version,
          params.id
        )(energiatodistus),
        perusparannuspassi: pppApi.deletePerusparannuspassi(
          fetch,
          perusparannuspassi.id
        )
      });
    } else if (perusparannuspassi && perusparannuspassi.id && showPPP) {
      // PPP exists and is visible - update it
      saveFuture = Future.parallelObject(2, {
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
      });
    } else {
      // No PPP or not modified - just save ET
      saveFuture = R.map(
        energiatodistus => ({ energiatodistus }),
        api.putEnergiatodistusById(
          fetch,
          params.version,
          params.id
        )(energiatodistus)
      );
    }

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

        // If PPP was deleted, reload the page to reset state
        if (pppMarkedForDeletion) {
          load(params);
        } else {
          onSuccessfulSave();
        }
      },
      R.chain(Future.after(400), saveFuture)
    );
  };

  // load energiatodistus and classifications in parallel
  const load = params => {
    console.log('load() called with params:', params);
    toggleOverlay(true);
    // form is recreated in reload - side effect is scroll to up
    resources = Maybe.None();
    perusparannuspassi = null;
    showPPP = false;
    pppMarkedForDeletion = false; // Reset deletion marker on load

    Future.fork(
      response => {
        console.log('Load failed:', response);
        toggleOverlay(false);
        announceError(i18n(Response.errorKey404(i18nRoot, 'load', response)));
      },
      response => {
        console.log('Load succeeded, response:', response);
        console.log(
          'energiatodistus perusparannuspassi-id:',
          response.energiatodistus['perusparannuspassi-id']
        );
        console.log(
          'response.perusparannuspassi:',
          response.perusparannuspassi
        );
        resources = Maybe.Some(response);

        // Set PPP if it exists and is valid
        if (
          response.perusparannuspassi &&
          response.perusparannuspassi.valid !== false
        ) {
          console.log('Setting PPP from response');
          perusparannuspassi = response.perusparannuspassi;
          showPPP = true;
        } else {
          console.log('No valid PPP in response');
          perusparannuspassi = Maybe.None();
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

  let energiatodistusFormComponent;
  const setFormDirty = () => {
    if (energiatodistusFormComponent) {
      energiatodistusFormComponent.$set({ dirty: true });
    }
  };
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as { energiatodistus, luokittelut, whoami, validation, valvonta, verkkolaskuoperaattorit, laskutusosoitteet }}
      <EnergiatodistusForm
        bind:this={energiatodistusFormComponent}
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
          <PPPSection
            {showPPP}
            onAddPPP={addPerusparannuspassi(energiatodistus.id)}
            onDeletePPP={deletePerusparannuspassi}>
            {#if perusparannuspassi}
              <div on:input={setFormDirty} on:change={setFormDirty}>
                <PPPForm
                  {energiatodistus}
                  inputLanguage={'fi'}
                  {luokittelut}
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
