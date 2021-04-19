<script>
  import { _ } from '@Language/i18n';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as api from './ohje-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1';
  import Link from '@Component/Link/Link';

  export let params;
  $: console.log('params', params);

  let overlay = true;
  let resources = Maybe.None();
  $: id = params.id;

  $: {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            'viesti.all.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
      },
      Future.parallelObject(2, {
        whoami: kayttajaApi.whoami,
        sivu: api.getSivu(id)
      })
    );
  }
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex">
    <div class="w-full flex flex-col">
      {#each Maybe.toArray(resources) as { sivu, whoami }}
        <div class="flex justify-between items-start">
          <H1 text={sivu.title} />
          {#if Kayttajat.isPaakayttaja(whoami)}
            <div class="mb-auto">
              <Link
                href={`/#/ohje/${sivu.id}/edit`}
                icon={Maybe.Some('edit')}
                text={'Edit_Page'} />
            </div>
          {/if}
        </div>
        <p class="whitespace-pre-wrap">{sivu.body}</p>
      {/each}
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
