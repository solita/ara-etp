<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import * as empty from '@Pages/energiatodistus/empty';
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
  import EtPppForm from '@Pages/energiatodistus/et-ppp-form.svelte';

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

  const submit = (energiatodistus, perusparannuspassi, onSuccessfulSave) => {
    toggleOverlay(true);

    Future.fork(
      response => {
        toggleOverlay(false);
        if (R.pathEq('missing-value', ['body', 'type'], response)) {
          showMissingProperties(response.body.missing);
        } else {
          announceError(i18n(Response.errorKey(i18nRoot, 'save', response)));
        }
      },
      ({ newPerusparannuspassiId }) => {
        toggleOverlay(false);
        announceSuccess($_('energiatodistus.messages.save-success'));
        onSuccessfulSave(newPerusparannuspassiId);
      },
      R.chain(
        Future.after(400),
        Future.parallelObject(2, {
          energiatodistus: api.putEnergiatodistusById(
            fetch,
            params.version,
            params.id
          )(energiatodistus),
          newPerusparannuspassiId: R.cond([
            [
              /* PPP has an ID so it has been submitted at least once */
              R.always(perusparannuspassi?.id),

              /* Always keep PPP state in sync with the local
               * (could well have no effect for a PPP that has valid: false) */
              () =>
                R.map(
                  R.always(Maybe.None()),
                  pppApi.putPerusparannuspassi(
                    fetch,
                    perusparannuspassi.id,
                    perusparannuspassi
                  )
                )
            ],
            [
              /* PPP has no ID but is marked as valid, so we are likely making a first save
               * for a newly added PPP */
              R.always(perusparannuspassi?.valid),
              () => {
                return R.map(
                  ppp => Maybe.Some(ppp.id),
                  pppApi.postPerusparannuspassi(fetch, perusparannuspassi)
                );
              }
            ],
            [R.T, () => Future.resolve(Maybe.None())]
          ])()
        })
      )
    );
  };

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
              perusparannuspassi: Maybe.fold(
                Future.resolve(
                  empty.perusparannuspassi(response.energiatodistus.id)
                ),
                pppId => pppApi.getPerusparannuspassi(fetch, pppId)
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
    {#each Maybe.toArray(resources) as { energiatodistus, luokittelut, validation, whoami, verkkolaskuoperaattorit, laskutusosoitteet, perusparannuspassi, pppValidation, valvonta }}
      <EtPppForm
        version={params.version}
        {energiatodistus}
        {perusparannuspassi}
        {luokittelut}
        {whoami}
        {validation}
        {valvonta}
        {verkkolaskuoperaattorit}
        {laskutusosoitteet}
        {pppValidation}
        bind:showMissingProperties
        {submit}
        title={title(energiatodistus)} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
