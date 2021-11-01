<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Response from '@Utility/response';

  import { _, locale } from '@Language/i18n';

  import Liitteet from '@Component/liitteet/liitteet.svelte';
  import * as Future from '@Utility/future-utils';
  import { flashMessageStore, idTranslateStore } from '@/stores';
  import * as ViestiApi from '@Pages/viesti/viesti-api';
  import Overlay from '../../components/Overlay/Overlay.svelte';
  import Spinner from '../../components/Spinner/Spinner.svelte';

  const i18nRoot = 'viesti.ketju.liitteet';
  const i18n = $_;

  export let params;
  let overlay = true;
  let dirty = true;
  let resources = Maybe.None();

  const load = params => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add('viesti', 'error',
          i18n(Response.errorKey404(i18nRoot, 'load', response)));
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        dirty = false;
        idTranslateStore.updateKetju(response.ketju, response.liitteet);
      },
      Future.parallelObject(5, {
        liitteet: ViestiApi.liitteet(params.id),
        ketju: ViestiApi.ketju(params.id)
      })
    );
  };

  load(params);

  const liiteOperation = (key, liiteFuture) => liite => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add('valvonta-oikeellisuus', 'error',
          i18n(Response.errorKey404(i18nRoot, key, response)));
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        load(params);
      },
      liiteFuture(liite)
    );
  }

  const liiteApi = {
    getUrl: ViestiApi.url.liitteet(params.id),

    addFiles: liiteOperation(
      'add-files',
      ViestiApi.postLiitteetFiles(params.id)
    ),

    addLink: liiteOperation(
      'add-link',
      ViestiApi.postLiitteetLink(params.id)
    ),

    deleteLiite: liiteOperation(
      'delete-liite',
      ViestiApi.deleteLiite(params.id)
    )
  };
</script>

{#each Maybe.toArray(resources) as {liitteet}}
  <Overlay {overlay}>
    <div slot="content" class="w-full mt-3">
    <Liitteet
      liiteApi={liiteApi}
      liitteet={liitteet}
      disabled={false}
      emptyMessageKey={i18nRoot + '.empty'}
      flashModule="viesti" />
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
{/each}