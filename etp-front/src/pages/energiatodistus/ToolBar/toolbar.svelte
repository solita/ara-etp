<script>
  import * as R from 'ramda';
  import { fade } from 'svelte/transition';

  import { replace, push } from 'svelte-spa-router';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as et from '@Pages/energiatodistus/energiatodistus-utils';

  import Confirm from '@Component/Confirm/Confirm';
  import Signing from '@Pages/energiatodistus/signing/SigningDialog.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Sisallysluettelo from './sisallysluettelo.svelte';

  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as PppApi from '@Pages/energiatodistus/perusparannuspassi-api';

  import * as Toolbar from './toolbar-utils';

  import { _ } from '@Language/i18n';
  import * as Response from '@Utility/response';
  import { announcementsForModule } from '@Utility/announce';

  export let energiatodistus;
  export let inputLanguage = 'fi';
  export let eTehokkuus = Maybe.None();
  export let dirty;
  export let whoami;

  const i18n = $_;
  const { announceError, announceSuccess } =
    announcementsForModule('Energiatodistus');

  const version = energiatodistus.versio;
  const id = Maybe.fromNull(energiatodistus.id);

  let energiatodistusKieli = Maybe.None();
  let bilingual = true;
  let selectedLanguage = 'fi';
  let signingActive = false;
  let pendingExecution = false;

  $: energiatodistusKieli = energiatodistus.perustiedot.kieli;
  $: bilingual = R.compose(
    Maybe.orSome(true),
    R.map(R.equals(et.kielisyys.bilingual))
  )(energiatodistusKieli);

  $: inputLanguage = bilingual
    ? selectedLanguage
    : Maybe.orSome(
        selectedLanguage,
        R.map(et.kielisyysKey, energiatodistusKieli)
      );

  function toggleLanguageSelection() {
    if (bilingual) {
      selectedLanguage = R.equals(selectedLanguage, 'fi') ? 'sv' : 'fi';
    }
  }

  let pdfUrls = [];

  $: if (bilingual) {
    pdfUrls = [
      R.map(i => ({ lang: 'fi', href: api.url.pdf(version, i, 'fi') }), id),
      R.map(i => ({ lang: 'sv', href: api.url.pdf(version, i, 'sv') }), id)
    ];
  } else {
    pdfUrls = [
      R.lift((i, k) => ({
        lang: et.kielisyysKey(k),
        href: api.url.pdf(version, i, et.kielisyysKey(k))
      }))(id, energiatodistusKieli)
    ];
  }

  const openSigning = _ => {
    signingActive = true;
  };

  const noop = () => {};

  const execute = (operation, name, onSuccess) => _ => {
    if (pendingExecution) return;
    pendingExecution = true;
    Future.fork(
      response => {
        pendingExecution = false;
        announceError(
          i18n(Response.errorKey404('energiatodistus', name, response))
        );
      },
      _ => {
        onSuccess();
        pendingExecution = false;
        announceSuccess(i18n(`energiatodistus.messages.${name}-success`));
      },
      operation(fetch, version, Maybe.get(id))
    );
  };

  const deleteEnergiatodistus = execute(
    api.deleteEnergiatodistus,
    'delete',
    _ => replace('/energiatodistus/all')
  );

  $: discardEnergiatodistus = execute(
    api.discardEnergiatodistus,
    'discard',
    cancel
  );

  $: undoDiscardEnergiatodistus = execute(
    api.undoDiscardEnergiatodistus,
    'undodiscard',
    cancel
  );

  export let save = _ => {};
  export let saveComplete = _ => {};
  export let cancel = _ => {};

  const openUrl = url => {
    window.open(url, '_blank');
  };

  $: fields = Toolbar.toolbarFields(whoami, R.prop('tila-id', energiatodistus));
</script>

<style type="text/postcss">
  button {
    @apply flex items-center gap-2 p-2;
  }

  button:last-of-type {
    @apply mb-3;
  }

  button:hover {
    @apply bg-primary text-light;
  }

  button:hover * {
    @apply border-light;
  }

  button:active {
    @apply bg-primarydark;
  }

  button.languageselect-button {
    @apply rounded-md border-4 border-ara-2021-basic-gray p-0;
  }

  button:disabled {
    @apply cursor-not-allowed text-disabled;
  }

  button:disabled:hover {
    @apply bg-light;
  }

  .description {
    @apply text-sm font-bold uppercase;
  }

  .languageselect {
    @apply w-1/2 py-1 font-bold uppercase text-light;
  }

  .languageselect:hover {
    @apply bg-primary;
  }

  .languageselect:active {
    @apply bg-primarydark;
  }

  .toolbar {
    max-height: 90vh;
    overflow: auto;
    align-items: flex-start;
  }

  .toolbar::-webkit-scrollbar {
    @apply w-2;
  }

  .toolbar::-webkit-scrollbar-track {
    @apply bg-background;
  }

  .toolbar::-webkit-scrollbar-thumb {
    @apply bg-disabled;
  }

  .toolbar::-webkit-scrollbar-thumb:hover {
    @apply bg-dark;
  }
</style>

{#if signingActive}
  <Signing {energiatodistus} reload={cancel} />
{/if}

<div class="toolbar relative flex flex-col text-secondary">
  <Sisallysluettelo {version} />
  {#if R.includes(Toolbar.module.save, fields)}
    <button disabled={!dirty || pendingExecution} on:click={save(noop)}>
      <span class="font-icon text-2xl">save</span>
      <span class="description">
        {id.isSome()
          ? i18n('energiatodistus.toolbar.save')
          : i18n('energiatodistus.toolbar.new')}
      </span>
    </button>
    <button disabled={!dirty || pendingExecution} on:click={cancel}>
      <span class="font-icon text-2xl">undo</span>
      <span class="description">{i18n('energiatodistus.toolbar.undo')}</span>
    </button>
  {/if}
  {#if R.includes(Toolbar.module.sign, fields)}
    <button
      disabled={pendingExecution}
      data-cy="allekirjoita-button"
      on:click={saveComplete(openSigning)}>
      <span class="border-b-3 border-secondary font-icon text-2xl">
        create
      </span>
      <div class="description">{i18n('energiatodistus.toolbar.sign')}</div>
    </button>
  {/if}
  {#if id.isSome() && Kayttajat.isLaatija(whoami)}
    <button
      disabled={pendingExecution}
      on:click={_ => {
        const newEtPage = _ =>
          push(
            '/energiatodistus/' + version + '/new?copy-from-id=' + id.some()
          );

        if (et.shouldSaveBeforeCopy(energiatodistus)) {
          save(newEtPage)();
        } else {
          newEtPage();
        }
      }}>
      <span class="font-icon text-2xl">file_copy</span>
      <span class="description">{i18n('energiatodistus.toolbar.copy')}</span>
    </button>
  {/if}
  {#if R.includes(Toolbar.module.preview, fields)}
    {#each pdfUrls as pdfUrl}
      {#each pdfUrl.toArray() as { href, lang }}
        <button
          disabled={pendingExecution}
          on:click={save(() => openUrl(href))}>
          <span class="font-icon text-2xl">picture_as_pdf</span>
          <span class="description block"
            >{i18n('energiatodistus.toolbar.preview')}
            {R.toUpper(lang)}</span>
        </button>
      {/each}
    {/each}
  {/if}
  {#if R.includes(Toolbar.module.download, fields)}
    {#each pdfUrls as pdfUrl}
      {#each pdfUrl.toArray() as { href, lang }}
        <button disabled={pendingExecution} on:click={() => openUrl(href)}>
          <span class="font-icon text-2xl">picture_as_pdf</span>
          <span class="description block"
            >{i18n('energiatodistus.toolbar.download')}
            {R.toUpper(lang)}</span>
        </button>
      {/each}
    {/each}
  {/if}
  {#if R.includes(Toolbar.module.discard, fields)}
    <Confirm
      let:confirm
      confirmButtonLabel={i18n('confirm.button.discard')}
      confirmMessage={i18n('confirm.you-want-to-discard')}>
      <button
        disabled={pendingExecution}
        on:click={() => confirm(discardEnergiatodistus)}>
        <span class="font-icon text-2xl">block</span>
        <span class="description"
          >{i18n('energiatodistus.toolbar.discard')}</span>
      </button>
    </Confirm>
  {/if}
  {#if R.includes(Toolbar.module.undodiscard, fields)}
    <Confirm
      let:confirm
      confirmButtonLabel={i18n('confirm.button.undodiscard')}
      confirmMessage={i18n('confirm.you-want-to-undodiscard')}>
      <button
        disabled={pendingExecution}
        on:click={() => confirm(undoDiscardEnergiatodistus)}>
        <span class="font-icon text-2xl">undo</span>
        <span class="description"
          >{i18n('energiatodistus.toolbar.undodiscard')}</span>
      </button>
    </Confirm>
  {/if}
  {#if R.includes(Toolbar.module.delete, fields) && Maybe.isSome(id)}
    <Confirm
      let:confirm
      confirmButtonLabel={i18n('confirm.button.delete')}
      confirmMessage={i18n('confirm.you-want-to-delete')}>
      <button
        disabled={pendingExecution}
        on:click={() => confirm(deleteEnergiatodistus)}>
        <span class="font-icon text-2xl">delete_forever</span>
        <span class="description"
          >{i18n('energiatodistus.toolbar.delete')}</span>
      </button>
    </Confirm>
  {/if}
  {#each eTehokkuus.toArray() as e}
    <div class="w-full border-2 border-dark bg-secondary py-2">
      <div class="pb-1 text-center text-sm font-bold text-light">
        {i18n('energiatodistus.tulokset.e-luku')}
        {e['e-luku']}
      </div>
      <div class="pt-1 text-center text-sm font-bold text-light">
        {i18n('energiatodistus.tulokset.e-luokka')}
        {e['e-luokka']}<sub>{energiatodistus.versio}</sub>
      </div>
    </div>
  {/each}
  {#if pendingExecution}
    <div
      transition:fade={{ duration: 50 }}
      class="absolute bottom-0 left-0 right-0 top-0 flex flex-wrap content-center justify-center bg-light opacity-75">
      <Spinner />
    </div>
  {/if}
  <div class="py-2">
    <div class="mb-2 w-full text-sm font-semibold text-dark">
      {i18n('energiatodistus.toolbar.language-label')}
    </div>
    <button on:click={toggleLanguageSelection} class="languageselect-button">
      {#if bilingual}
        <div class="flex w-full flex-row">
          {#each ['fi', 'sv'] as language}
            <div
              data-cy={`languageselect-${language}`}
              class="languageselect px-7"
              class:bg-primary={R.equals(selectedLanguage, language)}
              class:bg-ara-2021-basic-gray={!R.equals(
                selectedLanguage,
                language
              )}>
              {language}
            </div>
          {/each}
        </div>
      {:else}
        <div class="w-full bg-primary px-7 py-1 font-bold uppercase text-light">
          {energiatodistusKieli.map(et.kielisyysKey).some()}
        </div>
      {/if}
    </button>
  </div>
</div>
