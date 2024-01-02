<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import * as Router from '@Component/Router/router';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Links from './links';

  import * as ValvontaApi from './valvonta-api';
  import * as VirhetypeApi from './virhetypes/api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import ToimenpideForm from './toimenpide-form.svelte';
  import { announcementsForModule } from '@Utility/announce';

  export let params;
  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';
  const { announceError, announceSuccess } = announcementsForModule(
    'valvonta-oikeellisuus'
  );

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;

  $: toimenpide = Toimenpiteet.emptyToimenpide(parseInt(params['type-id']));

  Future.fork(
    response => {
      const msg = $_(
        `${i18nRoot}.messages.load-error`,
        Response.localizationKey(response)
      );

      announceError(msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(4, {
      whoami: KayttajaApi.whoami,
      virhetyypit: VirhetypeApi.virhetypes,
      severities: ValvontaApi.severities,
      templatesByType: ValvontaApi.templatesByType
    })
  );

  const addToimenpide = R.compose(
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            `${i18nRoot}.messages.add-error`,
            Response.localizationKey(response)
          )
        );
        announceError(msg);
        overlay = false;
      },
      response => {
        announceSuccess($_(`${i18nRoot}.messages.add-success`));
        dirty = false;
        Router.replace(Links.toimenpide(response, params));
      }
    ),
    R.tap(_ => {
      overlay = true;
    }),
    ValvontaApi.postToimenpide(params.id)
  );

  const cancel = _ => {
    toimenpide = Toimenpiteet.emptyToimenpide(parseInt(params['type-id']));
  };
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { whoami, templatesByType, virhetyypit, severities }}
      <ToimenpideForm
        {toimenpide}
        bind:dirty
        {whoami}
        {templatesByType}
        {virhetyypit}
        {severities}
        {cancel}
        submit={addToimenpide} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
