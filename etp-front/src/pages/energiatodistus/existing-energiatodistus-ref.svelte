<script>
  import * as R from 'ramda';

  import {_} from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as versionApi from '@Component/Version/version-api';

  import * as et from '@Pages/energiatodistus/energiatodistus-utils';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as pppApi from '@Pages/energiatodistus/perusparannuspassi-api';
  import * as ValvontaApi from '@Pages/valvonta-oikeellisuus/valvonta-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as laskutusApi from '@Utility/api/laskutus-api';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import {announcementsForModule} from '@Utility/announce';
  import EtPppForm from '@Pages/energiatodistus/EtPppForm.svelte';

  export let params;

  const i18n = $_;
  const i18nRoot = 'energiatodistus';
  const {announceError, announceSuccess} =
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

  // PPP state is stored on the server side for an existing energiatodistus.
  const addPerusparannuspassiForEnergiatodistus =
    R.curry(energiatodistusId => setPPP => () =>
      R.compose(
        Future.fork(
          () => R.compose(
            R.always(
              announceError(i18n('energiatodistus.messages.add-ppp-error'))
            ),
            setPPP
          )(Maybe.None()),
          R.compose(
            R.always(
              announceSuccess(i18n('energiatodistus.messages.add-ppp-success'))
            ),
            setPPP,
            Maybe.fromNull
          )
        ),
        Future.lastly(Future.attempt(() => toggleOverlay(false))),
        R.chain(pppApi.getPerusparannuspassi(fetch)),
        pppApi.addPerusparannuspassi(fetch),
        R.tap(() => toggleOverlay(true))
      )(energiatodistusId));

  // Delete PPP - deletes immediately via API
  const deletePerusparannuspassi = R.curry((setPPP, currentMaybePerusparannuspassi) => () =>
    Maybe.cata(
      // We should never be deleting a non-existing perusparannuspassi but we'll just set it to None then.
      () => setPPP(Maybe.None()),
      R.compose(
        Future.fork(
          _response => announceError(i18n('energiatodistus.messages.delete-ppp-error')),
          _success => {
            setPPP(Maybe.None());
            announceSuccess(i18n('energiatodistus.messages.mark-ppp-for-deletion'));
          }
        ),
        Future.lastly(Future.attempt(() => toggleOverlay(false))),
        R.chain(pppId => pppApi.deletePerusparannuspassi(fetch, pppId)),
        // Save the current state before deleting and take the pppId forward.
        ppp => R.map(_ => ppp.id, pppApi.putPerusparannuspassi(fetch, ppp.id, ppp)),
        R.tap(() => toggleOverlay(true))
      )
    )(currentMaybePerusparannuspassi));

  const submit = R.curry((energiatodistus, maybePerusparannuspassi, onSuccessfulSave) =>
    R.compose(
      Future.fork(
        // TODO: AE-2690: Should showing missing properties work for new-energiatodistus as well?
        response => {
          if (R.pathEq('missing-value', ['body', 'type'], response)) {
            showMissingProperties(response.body.missing);
          } else {
            announceError(i18n(Response.errorKey(i18nRoot, 'save', response)));
          }
        },
        _result => {
          announceSuccess($_('energiatodistus.messages.save-success'));
          onSuccessfulSave();
        }
      ),
      Future.lastly(Future.attempt(() => toggleOverlay(false))),
      R.chain(Future.after(400)),
      futures => Future.parallel(R.length(futures), futures),
      R.concat([api.putEnergiatodistusById(fetch, params.version, params.id, energiatodistus)]),
      R.map(ppp => pppApi.putPerusparannuspassi(fetch, ppp.id, ppp)),
      R.tap(x => console.log(x)),
      Maybe.toArray,
      R.tap(() => toggleOverlay(true))
    )(maybePerusparannuspassi));

  // load energiatodistus and classifications in parallel
  const load = params => {
    toggleOverlay(true);
    // form is recreated in reload - side effect is scroll to up
    resources = Maybe.None();

    Future.fork(
      response => {
        console.log('Load failed:', response);
        toggleOverlay(false);
        announceError(i18n(Response.errorKey404(i18nRoot, 'load', response)));
      },
      response => {
        resources = Maybe.Some(response);
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
              maybePerusparannuspassi:
                Maybe.cata(
                  () => Future.resolve(Maybe.None()),
                  pppId => R.map(Maybe.of, pppApi.getPerusparannuspassi(fetch, pppId))
                )(pppId)
            })
          );
        },
        Future.parallelObject(7, {
          energiatodistus: api.getEnergiatodistusById(
            params.version,
            params.id
          ),
          luokittelut: api.luokittelutForVersion(params.version),
          whoami: kayttajaApi.whoami,
          validation: api.validation(params.version),
          pppValidation: pppApi.pppValidation(params.version),
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
    {#each Maybe.toArray(resources) as {
      energiatodistus,
      luokittelut,
      validation,
      whoami,
      verkkolaskuoperaattorit,
      laskutusosoitteet,
      maybePerusparannuspassi,
      pppValidation,
      valvonta
    }}
      <EtPppForm
        version={params.version}
        {energiatodistus}
        {maybePerusparannuspassi}
        {luokittelut}
        {whoami}
        {validation}
        {valvonta}
        {verkkolaskuoperaattorit}
        {laskutusosoitteet}
        {pppValidation}
        bind:showMissingProperties
        {submit}
        title={title(energiatodistus)}

        addPerusparannuspassi={addPerusparannuspassiForEnergiatodistus(energiatodistus.id)}
        {deletePerusparannuspassi}

      />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner/>
  </div>
</Overlay>
