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
  import TyojonoButton from './tyojono-button';
  import Spinner from '@Component/Spinner/Spinner.svelte';

  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as ValvontaApi from '@Pages/valvonta-oikeellisuus/valvonta-api';

  import * as Toolbar from './toolbar-utils';

  import { _ } from '@Language/i18n';
  import * as Response from '@Utility/response';
  import { announcementsForModule } from '@Utility/announce';

  export let energiatodistus;
  export let inputLanguage = 'fi';
  export let eTehokkuus = Maybe.None();
  export let dirty;
  export let whoami;
  export let valvonta = { ongoing: false, pending: false };

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

  $: toggleTyojono = execute(
    (fetch, _, id) =>
      R.chain(
        Future.after(200),
        ValvontaApi.putValvonta(id, { pending: !valvonta.pending })
      ),
    `tyojono-${!valvonta.pending ? 'add' : 'remove'}`,
    _ => {
      valvonta = R.over(R.lensProp('pending'), R.not, valvonta);
    }
  );

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
    @apply w-32 flex flex-col justify-center items-center;
  }

  button:not(:first-child) {
    @apply h-24;
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

  button:disabled {
    @apply text-disabled cursor-not-allowed;
  }

  button:disabled:hover {
    @apply bg-light;
  }

  .description {
    @apply font-bold uppercase text-sm;
  }

  .languageselect {
    @apply font-bold uppercase text-light w-1/2 py-2;
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
  <Signing
    {energiatodistus}
    reload={cancel}
    checkIfSelectionIsAllowed={true}
    {whoami} />
{/if}

<div
  class="toolbar relative flex flex-col text-secondary border-1 border-disabled">
  <button on:click={toggleLanguageSelection}>
    {#if bilingual}
      <div class="flex flex-row w-full">
        {#each ['fi', 'sv'] as language}
          <div
            data-cy={`languageselect-${language}`}
            class="languageselect"
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
      <div class="w-full font-bold py-2 uppercase text-light bg-primary">
        {energiatodistusKieli.map(et.kielisyysKey).some()}
      </div>
    {/if}
  </button>
  {#if R.includes(Toolbar.module.tyojono, fields)}
    <TyojonoButton
      disabled={pendingExecution}
      {valvonta}
      on:click={toggleTyojono} />
  {/if}
  {#if R.includes(Toolbar.module.save, fields)}
    <button disabled={!dirty || pendingExecution} on:click={save(noop)}>
      <span class="description">
        {id.isSome()
          ? i18n('energiatodistus.toolbar.save')
          : i18n('energiatodistus.toolbar.new')}
      </span>
      <span class="text-2xl font-icon">save</span>
    </button>
    <button disabled={!dirty || pendingExecution} on:click={cancel}>
      <span class="description">{i18n('energiatodistus.toolbar.undo')}</span>
      <span class="text-2xl font-icon">undo</span>
    </button>
  {/if}
  {#if R.includes(Toolbar.module.sign, fields)}
    <button
      disabled={pendingExecution}
      data-cy="allekirjoita-button"
      on:click={saveComplete(openSigning)}>
      <div class="description">{i18n('energiatodistus.toolbar.sign')}</div>
      <span class="text-2xl font-icon border-b-3 border-secondary">
        create
      </span>
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
      <span class="description">{i18n('energiatodistus.toolbar.copy')}</span>
      <span class="text-2xl font-icon">file_copy</span>
    </button>
  {/if}
  {#if R.includes(Toolbar.module.preview, fields)}
    {#each pdfUrls as pdfUrl}
      {#each pdfUrl.toArray() as { href, lang }}
        <button
          disabled={pendingExecution}
          on:click={save(() => openUrl(href))}>
          <span class="block description"
            >{i18n('energiatodistus.toolbar.preview')}
            {R.toUpper(lang)}</span>
          <span class="text-2xl font-icon">picture_as_pdf</span>
        </button>
      {/each}
    {/each}
  {/if}
  {#if R.includes(Toolbar.module.download, fields)}
    {#each pdfUrls as pdfUrl}
      {#each pdfUrl.toArray() as { href, lang }}
        <button disabled={pendingExecution} on:click={() => openUrl(href)}>
          <span class="block description"
            >{i18n('energiatodistus.toolbar.download')}
            {R.toUpper(lang)}</span>
          <span class="text-2xl font-icon">picture_as_pdf</span>
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
        <span class="description"
          >{i18n('energiatodistus.toolbar.discard')}</span>
        <span class="text-2xl font-icon">block</span>
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
        <span class="description"
          >{i18n('energiatodistus.toolbar.undodiscard')}</span>
        <span class="text-2xl font-icon">undo</span>
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
        <span class="description"
          >{i18n('energiatodistus.toolbar.delete')}</span>
        <span class="text-2xl font-icon">delete_forever</span>
      </button>
    </Confirm>
  {/if}
  {#each eTehokkuus.toArray() as e}
    <div class="border-2 border-dark py-2 bg-secondary">
      <div class="font-bold text-center text-sm text-light pb-1">
        {i18n('energiatodistus.tulokset.e-luku')}
        {e['e-luku']}
      </div>
      <div class="font-bold text-center text-sm text-light pt-1">
        {i18n('energiatodistus.tulokset.e-luokka')}
        {e['e-luokka']}<sub>{energiatodistus.versio}</sub>
      </div>
    </div>
  {/each}
  {#if pendingExecution}
    <div
      transition:fade={{ duration: 50 }}
      class="absolute bg-light opacity-75 top-0 bottom-0 left-0 right-0 flex flex-wrap justify-center content-center">
      <Spinner />
    </div>
  {/if}
</div>
