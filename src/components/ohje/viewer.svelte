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
  import Navigation from './navigation';
  import DOMPurify from 'dompurify';
  import Marked from 'marked';
  import Style from '@Component/text-editor/style.svelte';

  export let params;

  $: id = params.id;

  const i18n = $_;
  let overlay = true;
  let resources = Maybe.None();

  $: {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
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

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex space-x-4">
    <div class="w-2/6 max-w-xs">
      <Navigation {id} />
    </div>
    <div class="w-4/6 flex-grow flex flex-col">
      {#each Maybe.toArray(resources) as { sivu, whoami }}
        <div class="flex justify-between items-start">
          <div class="flex items-start justify-start">
            <H1 text={sivu.title} />
            {#if !sivu.published}
              <span
                class="font-icon text-xl select-none ml-1 text-error"
                title={i18n('ohje.viewer.unpublished')}>
                visibility_off
              </span>
            {/if}
          </div>
          {#if Kayttajat.isPaakayttaja(whoami)}
            <div class="mb-auto font-semibold">
              <Link
                href={`/#/ohje/${sivu.id}/edit`}
                icon={Maybe.Some('edit')}
                text={i18n('ohje.viewer.edit')} />
            </div>
          {/if}
        </div>
        <p class=" whitespace-pre-wrap">
          <Style>
            {@html Marked(DOMPurify.sanitize(sivu.body))}
          </Style>
        </p>
      {/each}
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
