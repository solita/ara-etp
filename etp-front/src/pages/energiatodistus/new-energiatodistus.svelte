<script>
  import { replace } from '@Component/Router/router';
  import { querystring } from 'svelte-spa-router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import qs from 'qs';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import EnergiatodistusForm from '@Pages/energiatodistus/EnergiatodistusForm';
  import PPPForm from '@Pages/energiatodistus/ppp-form.svelte';
  import PPPSection from '@Pages/energiatodistus/PPPSection.svelte';
  import * as empty from '@Pages/energiatodistus/empty';
  import * as ET from '@Pages/energiatodistus/energiatodistus-utils';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as pppApi from '@Pages/energiatodistus/perusparannuspassi-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as laskutusApi from '@Utility/api/laskutus-api';
  import * as versionApi from '@Component/Version/version-api';
  import { isEtp2026Enabled } from '@Utility/config_utils.js';
  import * as Schema from '@Pages/energiatodistus/schema';

  import * as Response from '@Utility/response';
  import { announcementsForModule } from '@Utility/announce';

  export let params;
  const i18n = $_;
  const i18nRoot = 'energiatodistus';
  const { announceError, announceSuccess } =
    announcementsForModule('Energiatodistus');

  let overlay = false;

  const toggleOverlay = value => {
    overlay = value;
  };

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

  // Add PPP - creates a local empty perusparannuspassi that will be saved with the energiatodistus
  const addPerusparannuspassi = () => {
    if (Maybe.isNone(perusparannuspassi)) {
      // Create empty PPP locally - energiatodistus doesn't have ID yet, will be set during save
      perusparannuspassi = Maybe.Some(empty.perusparannuspassi(null));
    }
  };

  const emptyEnergiatodistus = versio =>
    R.cond([
      [R.equals('2018'), () => empty.energiatodistus2018()],
      [R.equals('2013'), () => empty.energiatodistus2013()],
      [R.equals('2026'), () => empty.energiatodistus2026()]
    ])(versio);

  const cleanEnergiatodistusCopy = R.compose(
    R.assoc('korvattu-energiatodistus-id', Maybe.None()),
    R.assoc('laskutettava-yritys-id', Maybe.None()),
    R.assoc('laskutusosoite-id', Maybe.None()),
    R.assoc('laskuriviviite', Maybe.None()),
    R.assoc('laatija-id', Maybe.None()),
    R.dissoc('laatija-fullname'),
    R.assoc('kommentti', Maybe.None()),
    R.assoc('tila-id', ET.tila.draft),
    R.dissoc('id')
  );

  $: copyFromId = R.compose(
    Maybe.fromNull,
    R.prop('copy-from-id'),
    qs.parse
  )($querystring);

  let resources = Maybe.None();

  const submit = (energiatodistus, onSuccessfulSave) =>
    R.compose(
      Future.fork(
        response => {
          toggleOverlay(false);
          announceError(i18n(Response.errorKey(i18nRoot, 'load', response)));
        },
        ({ energiatodistus }) => {
          toggleOverlay(false);
          announceSuccess($_('energiatodistus.messages.save-success'));
          onSuccessfulSave();
          replace(`/energiatodistus/${params.version}/${energiatodistus.id}`);
        }
      ),
      R.chain(Future.after(400)),
      R.when(
        () => Maybe.isSome(perusparannuspassi),
        R.chain(etResult =>
          R.map(
            pppResult => ({
              energiatodistus: etResult,
              perusparannuspassi: pppResult
            }),
            pppApi.postPerusparannuspassi(
              fetch,
              R.assoc(
                'energiatodistus-id',
                etResult.id,
                perusparannuspassi.some()
              )
            )
          )
        )
      ),
      api.postEnergiatodistus(fetch, params.version),
      R.tap(() => toggleOverlay(true))
    )(energiatodistus);

  $: title =
    $_('energiatodistus.title') +
    ' ' +
    params.version +
    ' - ' +
    Maybe.fold(
      $_('energiatodistus.new.draft'),
      id => $_('energiatodistus.new.copy-from') + ' ' + id,
      copyFromId
    );

  // Load all page resources
  const load = (version, copyFromId) => {
    toggleOverlay(true);
    Future.fork(
      response => {
        toggleOverlay(false);
        announceError(i18n(Response.errorKey(i18nRoot, 'load', response)));
      },
      response => {
        resources = Maybe.Some(response);
        toggleOverlay(false);
      },
      R.chain(
        response =>
          R.map(
            R.assoc('laskutusosoitteet', R.__, response),
            laatijaApi.laskutusosoitteet(response.whoami.id)
          ),
        Future.parallelObject(6, {
          energiatodistus: Maybe.fold(
            Future.resolve(emptyEnergiatodistus(version)),
            R.compose(
              R.map(cleanEnergiatodistusCopy),
              api.getEnergiatodistusById(version)
            ),
            copyFromId
          ),
          luokittelut: api.luokittelutForVersion(version),
          whoami: kayttajaApi.whoami,
          validation: api.validation(version),
          verkkolaskuoperaattorit: laskutusApi.verkkolaskuoperaattorit
        })
      )
    );
  };

  $: load(params.version, copyFromId);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each resources.toArray() as { energiatodistus, luokittelut, validation, whoami, verkkolaskuoperaattorit, laskutusosoitteet }}
      <EnergiatodistusForm
        version={params.version}
        {title}
        {energiatodistus}
        {whoami}
        {luokittelut}
        {validation}
        {verkkolaskuoperaattorit}
        {laskutusosoitteet}
        {submit}>
        <!-- PPP section as slot content -->
        {#if isEtp2026Enabled(config) && params.version == 2026}
          <PPPSection
            showPPP={Maybe.isSome(perusparannuspassi)}
            onAddPPP={addPerusparannuspassi}>
            {#each Maybe.toArray(perusparannuspassi) as ppp}
              <PPPForm
                {energiatodistus}
                inputLanguage={'fi'}
                {luokittelut}
                bind:perusparannuspassi={ppp}
                schema={Schema.perusparannuspassi} />
            {/each}
          </PPPSection>
        {/if}
      </EnergiatodistusForm>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
