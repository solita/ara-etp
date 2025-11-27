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

  // Add PPP - creates a new perusparannuspassi via API
  const addPerusparannuspassi = energiatodistusId => () => {
    // If PPP already exists but is hidden, just show it
    if (perusparannuspassi && !Maybe.isNone(perusparannuspassi)) {
      showPPP = !showPPP;
      return;
    }

    // Create a new PPP via API (backend will resurrect soft-deleted PPP if exists)
    if (!showPPP && (!perusparannuspassi || Maybe.isNone(perusparannuspassi))) {
      toggleOverlay(true);
      Future.fork(
        _response => {
          toggleOverlay(false);
          announceError(i18n('energiatodistus.messages.add-ppp-error'));
        },
        id => {
          // After creation, fetch the full PPP object
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
            pppApi.getPerusparannuspassi(fetch, id)
          );
        },
        pppApi.addPerusparannuspassi(fetch, energiatodistusId)
      );
    }
  };

  // Delete PPP - deletes immediately via API
  const deletePerusparannuspassi = () => {
    if (perusparannuspassi && perusparannuspassi.id) {
      toggleOverlay(true);
      Future.fork(
        _response => {
          toggleOverlay(false);
          announceError(i18n('energiatodistus.messages.delete-ppp-error'));
        },
        _success => {
          perusparannuspassi = Maybe.None();
          showPPP = false;
          toggleOverlay(false);
          announceSuccess(i18n('energiatodistus.messages.mark-ppp-for-deletion'));
        },
        pppApi.deletePerusparannuspassi(fetch, perusparannuspassi.id)
      );
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
      result => {
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
    perusparannuspassi = Maybe.None();
    showPPP = false;

    Future.fork(
      response => {
        console.log('Load failed:', response);
        toggleOverlay(false);
        announceError(i18n(Response.errorKey404(i18nRoot, 'load', response)));
      },
      response => {
        resources = Maybe.Some(response);

        // Set PPP if it exists and is valid
        if (
          response.perusparannuspassi &&
          response.perusparannuspassi.valid !== false
        ) {
          perusparannuspassi = response.perusparannuspassi;
          showPPP = true;
        } else {
          perusparannuspassi = Maybe.None();
          showPPP = false;
        }

        toggleOverlay(false);
      },
      R.chain(
        response => {
          const pppId = response.energiatodistus['perusparannuspassi-id'];

          return R.map(
            R.mergeLeft(response),
            Future.parallelObject(2, {
              laskutusosoitteet: laatijaApi.laskutusosoitteet(
                Maybe.get(response.energiatodistus['laatija-id'])
              ),
              perusparannuspassi:
                pppId && Maybe.isSome(pppId)
                  ? pppApi.getPerusparannuspassi(fetch, Maybe.get(pppId))
                  : Future.resolve(null)
            })
          );
        },
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
