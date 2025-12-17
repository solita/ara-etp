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
  import * as empty from '@Pages/energiatodistus/empty';
  import * as ET from '@Pages/energiatodistus/energiatodistus-utils';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as pppApi from '@Pages/energiatodistus/perusparannuspassi-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as laskutusApi from '@Utility/api/laskutus-api';
  import * as versionApi from '@Component/Version/version-api';

  import * as Response from '@Utility/response';
  import { announcementsForModule } from '@Utility/announce';
  import EtPppForm from '@Pages/energiatodistus/EtPppForm.svelte';

  export let params;
  const i18n = $_;
  const i18nRoot = 'energiatodistus';
  const { announceError, announceSuccess } =
    announcementsForModule('Energiatodistus');

  let overlay = false;

  const toggleOverlay = value => {
    overlay = value;
  };

  // TODO: AE-2690: Do we want this for new?
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

  const submit = (energiatodistus, perusparannuspassi, onSuccessfulSave) => {
    const onUnsuccessfulResponse = response => {
      toggleOverlay(false);
      announceError(i18n(Response.errorKey(i18nRoot, 'load', response)));
    };

    const onSuccesfulResponse = etResult => {
      toggleOverlay(false);
      announceSuccess($_('energiatodistus.messages.save-success'));
      onSuccessfulSave();
      replace(`/energiatodistus/${params.version}/${etResult.id}`);
    };

    let future;
    if (R.equals(true, perusparannuspassi.valid)) {
      future = R.chain(
        et =>
          R.map(
            R.always(et),
            pppApi.postPerusparannuspassi(
              fetch,
              R.assoc('energiatodistus-id', et.id, perusparannuspassi)
            )
          ),
        api.postEnergiatodistus(fetch, params.version, energiatodistus)
      );
    } else {
      future = api.postEnergiatodistus(fetch, params.version, energiatodistus);
    }

    toggleOverlay(true);
    return Future.fork(onUnsuccessfulResponse, onSuccesfulResponse, future);
  };

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
        Future.parallelObject(7, {
          perusparannuspassi: Future.resolve(empty.perusparannuspassi(null)),
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
          pppValidation: pppApi.pppValidation(version),
          verkkolaskuoperaattorit: laskutusApi.verkkolaskuoperaattorit
        })
      )
    );
  };

  $: load(params.version, copyFromId);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each resources.toArray() as { energiatodistus, luokittelut, validation, whoami, verkkolaskuoperaattorit, laskutusosoitteet, perusparannuspassi, pppValidation }}
      <EtPppForm
        version={params.version}
        {title}
        {energiatodistus}
        {luokittelut}
        {validation}
        {whoami}
        {verkkolaskuoperaattorit}
        {laskutusosoitteet}
        {perusparannuspassi}
        {pppValidation}
        {submit}
        bind:showMissingProperties />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
