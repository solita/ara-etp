<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';

  import { flashMessageStore } from '@/stores';
  import * as Router from '@Component/Router/router';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Links from './links';

  import * as ValvontaApi from './valvonta-api';
  import * as KayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import ToimenpideForm from './toimenpide-form.svelte';

  export let params;
  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;

  $: toimenpide = Toimenpiteet.emptyToimenpide(parseInt(params['type-id']));

  Future.fork(
    response => {
      const msg = $_(`${i18nRoot}.messages.load-error`,
        Response.localizationKey(response));

      flashMessageStore.add('viesti', 'error', msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(2, {
      whoami: KayttajaApi.whoami,
      templatesByType: ValvontaApi.templatesByType,
    })
  );

  const addToimenpide = R.compose(
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
        overlay = false;
      },
      response => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'success',
          $_(`${i18nRoot}.messages.success`)
        );
        dirty = false;
        Router.replace(Links.toimenpide(response, params))
      }
    ),
    R.tap(_ => { overlay = true; }),
    ValvontaApi.postToimenpide(params.id)
  );

  const cancel = _ => {
    toimenpide = Toimenpiteet.emptyToimenpide(parseInt(params['type-id']));
  }
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <DirtyConfirmation {dirty}/>
    {#each Maybe.toArray(resources) as {whoami, templatesByType}}
      <ToimenpideForm {toimenpide}
                      bind:dirty
                      {whoami}
                      {templatesByType}
                      {cancel}
                      submit={addToimenpide}/>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner/>
  </div>
</Overlay>