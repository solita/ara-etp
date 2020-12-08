<script>
  import * as R from 'ramda';

  import { replace } from 'svelte-spa-router';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as et from '@Component/Energiatodistus/energiatodistus-utils';

  import Confirm from '@Component/Confirm/Confirm';
  import Signing from '@Component/Energiatodistus/signing';
  import * as api from '@Component/Energiatodistus/energiatodistus-api';

  import * as Toolbar from './toolbar-utils';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  export let energiatodistus;
  export let inputLanguage = 'fi';
  export let eTehokkuus = Maybe.None();
  export let dirty;
  export let whoami;

  const version = energiatodistus.versio;
  const id = Maybe.fromNull(energiatodistus.id);

  let energiatodistusKieli = Maybe.None();
  let bilingual = true;
  let selectedLanguage = 'fi';
  let signingActive = false;

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

  const deleteEnergiatodistus = () => {
    Future.fork(
      _ =>
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          $_('energiatodistukset.messages.delete-error')
        ),
      _ => {
        replace('/energiatodistus/all');
        flashMessageStore.add(
          'Energiatodistus',
          'success',
          $_('energiatodistukset.messages.delete-success')
        );
      },
      api.deleteEnergiatodistus(fetch, version, Maybe.get(id))
    );
  };

  export let save = _ => {};
  export let saveComplete = _ => {};
  export let cancel = _ => {};

  const openUrl = url => {
    window.open(url, '_blank');
  };

  const noop = () => {};

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

<!-- purgecss: bg-primary bg-disabled -->

{#if signingActive}
  <Signing {energiatodistus} reload={cancel} />
{/if}

<div class="toolbar flex flex-col text-secondary border-1 border-disabled">
  <button on:click={toggleLanguageSelection}>
    {#if bilingual}
      <div class="flex flex-row w-full">
        {#each ['fi', 'sv'] as language}
          <div
            class="languageselect"
            class:bg-primary={R.equals(selectedLanguage, language)}
            class:bg-disabled={!R.equals(selectedLanguage, language)}>
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
  {#if R.includes(Toolbar.module.save, fields)}
    <button disabled={!dirty} on:click={save(noop)}>
      <span class="description">
        {id.isSome() ? $_('energiatodistus.toolbar.save') : $_('energiatodistus.toolbar.new')}
      </span>
      <span class="text-2xl font-icon">save</span>
    </button>
    <button disabled={!dirty} on:click={cancel}>
      <span class="description">{$_('energiatodistus.toolbar.undo')}</span>
      <span class="text-2xl font-icon">undo</span>
    </button>
  {/if}
  {#if R.includes(Toolbar.module.sign, fields)}
    <button on:click={saveComplete(openSigning)}>
      <div class="description">{$_('energiatodistus.toolbar.sign')}</div>
      <span class="text-2xl font-icon border-b-3 border-secondary">
        create
      </span>
    </button>
  {/if}
  {#if R.includes(Toolbar.module.copy, fields)}
    {#if id.isSome()}
      <button>
        <span class="description">{$_('energiatodistus.toolbar.copy')}</span>
        <span class="text-2xl font-icon">file_copy</span>
      </button>
    {/if}
  {/if}
  {#if R.includes(Toolbar.module.preview, fields)}
    {#each pdfUrls as pdfUrl}
      {#each pdfUrl.toArray() as { href, lang }}
        <button on:click={save(() => openUrl(href))}>
          <span
            class="block description">{$_('energiatodistus.toolbar.preview')}
            {R.toUpper(lang)}</span>
          <span class="text-2xl font-icon">picture_as_pdf</span>
        </button>
      {/each}
    {/each}
  {/if}
  {#if R.includes(Toolbar.module.download, fields)}
    {#each pdfUrls as pdfUrl}
      {#each pdfUrl.toArray() as { href, lang }}
        <button on:click={() => openUrl(href)}>
          <span
            class="block description">{$_('energiatodistus.toolbar.download')}
            {R.toUpper(lang)}</span>
          <span class="text-2xl font-icon">picture_as_pdf</span>
        </button>
      {/each}
    {/each}
  {/if}
  {#if R.includes(Toolbar.module.discard, fields)}
    <Confirm
      let:confirm
      confirmButtonLabel={$_('confirm.button.discard')}
      confirmMessage={$_('confirm.you-want-to-discard')}>
      <button on:click={() => confirm(() => {})}>
        <span class="description">{$_('energiatodistus.toolbar.discard')}</span>
        <span class="text-2xl font-icon">block</span>
      </button>
    </Confirm>
  {/if}
  {#if R.includes(Toolbar.module.undodiscard, fields)}
    <Confirm
      let:confirm
      confirmButtonLabel={$_('confirm.button.undodiscard')}
      confirmMessage={$_('confirm.you-want-to-discard')}>
      <button on:click={() => confirm(() => {})}>
        <span
          class="description">{$_('energiatodistus.toolbar.undodiscard')}</span>
        <span class="text-2xl font-icon">undo</span>
      </button>
    </Confirm>
  {/if}
  {#if R.includes(Toolbar.module.delete, fields)}
    <Confirm
      let:confirm
      confirmButtonLabel={$_('confirm.button.delete')}
      confirmMessage={$_('confirm.you-want-to-delete')}>
      <button on:click={() => confirm(deleteEnergiatodistus)}>
        <span class="description">{$_('energiatodistus.toolbar.delete')}</span>
        <span class="text-2xl font-icon">delete_forever</span>
      </button>
    </Confirm>
  {/if}
  {#each eTehokkuus.toArray() as e}
    <div class="border-2 border-dark py-2 bg-secondary">
      <div class="font-bold text-center text-sm text-light pb-1">
        {$_('energiatodistus.tulokset.e-luku')}
        {e['e-luku']}
      </div>
      <div class="font-bold text-center text-sm text-light pt-1">
        {$_('energiatodistus.tulokset.e-luokka')}
        {e['e-luokka']}<sub>{energiatodistus.versio}</sub>
      </div>
    </div>
  {/each}
</div>
