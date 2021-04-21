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

  $: id = params.id;

  let overlay = true;
  let resources = Maybe.None();

  $: {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome('ohje.load-error', Response.localizationKey(response))
        );

        flashMessageStore.add('ohje', 'error', msg);
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
          <div class="flex items-start justify-start">
            <H1 text={sivu.title} />
            {#if !sivu.published}
              <span
                class="material-icons select-none ml-1 text-error"
                title={$_(`ohje.unpublished`)}>
                visibility_off
              </span>
            {/if}
          </div>
          {#if Kayttajat.isPaakayttaja(whoami)}
            <div class="mb-auto font-semibold">
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
