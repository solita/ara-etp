<script>
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import Liitteet from '@Component/liitteet/liitteet.svelte';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  const i18nRoot = 'energiatodistus.liitteet';
  const i18n = $_;

  export let params;
  let overlay = true;
  let dirty = true;
  let resources = Maybe.None();

  const load = params => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          i18n(Response.errorKey404(i18nRoot, 'load', response))
        );
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        dirty = false;
      },
      Future.parallelObject(2, {
        liitteet: api.liitteet(params.version, params.id),
        whoami: KayttajaApi.whoami
      })
    );
  };

  load(params);

  const liiteOperation = (key, liiteFuture) => liite => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          i18n(Response.errorKey404(i18nRoot, key, response))
        );
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'Energiatodistus',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        load(params);
      },
      liiteFuture(liite)
    );
  };

  const liiteApi = {
    getUrl: api.url.liitteet(params.version, params.id),

    addFiles: liiteOperation(
      'add-files',
      api.postLiitteetFiles(params.version, params.id)
    ),

    addLink: liiteOperation('add-link', api.postLiitteetLink(params.version, params.id)),

    deleteLiite: liiteOperation(
      'delete-liite',
      api.deleteLiite(params.version, params.id)
    )
  };
</script>

{#each Maybe.toArray(resources) as { liitteet, whoami }}
  <Overlay {overlay}>
    <div slot="content" class="w-full mt-3">
      <Liitteet
        {liiteApi}
        {liitteet}
        disabled={!Kayttajat.isPaakayttaja(whoami)}
        emptyMessageKey={i18nRoot + '.empty'}
        flashModule="viesti" />
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
{/each}
