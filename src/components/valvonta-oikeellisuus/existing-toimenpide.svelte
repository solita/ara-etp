<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';

  import { flashMessageStore, idTranslateStore } from '@/stores';
  import { replace } from '@Component/Router/router';

  import * as Toimenpiteet from './toimenpiteet';

  import * as ValvontaApi from './valvonta-api';
  import * as KayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import ToimenpideForm from './toimenpide-form.svelte';

  export let params;
  const i18nRoot = 'valvonta.oikeellisuus.new-toimenpide';

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;

  const load = params => {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(`${i18nRoot}.messages.load-error`,
          Response.localizationKey(response));

        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        dirty = false;
      },
      Future.parallelObject(2, {
        whoami: KayttajaApi.whoami,
        toimenpide: ValvontaApi.toimenpide(params.id, params['toimenpide-id'])
      })
    )
  };

  $: load(params);

  const updateToimenpide = successCallback => R.compose(
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
          $_(`${i18nRoot}.messages.save-success`)
        );
        successCallback();
        load(params);
      }
    ),
    R.tap(_ => { overlay = true; }),
    ValvontaApi.putToimenpide(params.id, params['toimenpide-id'])
  );

  const publishToimenpide = _ => Future.fork(
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
      _ => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'success',
          $_(`${i18nRoot}.messages.publish-success`));
      },
      ValvontaApi.publishToimenpide(params.id, params['toimenpide-id']));

  const saveToimenpide = updateToimenpide(_ => {});
  const saveAndPublishToimenpide = updateToimenpide(publishToimenpide);

  const cancel = _ => {
    load(params);
  }
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <DirtyConfirmation {dirty}/>
    {#each Maybe.toArray(resources) as {whoami, toimenpide}}
      <ToimenpideForm {toimenpide}
                      bind:dirty
                      {whoami}
                      {cancel}
                      submit={saveToimenpide}
                      publish={Maybe.Some(saveAndPublishToimenpide)}/>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner/>
  </div>
</Overlay>