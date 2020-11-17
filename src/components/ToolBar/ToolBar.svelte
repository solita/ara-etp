<script>
  import * as R from 'ramda';

  import { replace, push } from 'svelte-spa-router';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as et from '@Component/Energiatodistus/energiatodistus-utils';

  import Confirm from '@Component/Confirm/Confirm';
  import Signing from '@Component/Energiatodistus/signing';
  import * as api from '@Component/Energiatodistus/energiatodistus-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  export let energiatodistus;
  export let inputLanguage = 'fi';
  export let eTehokkuus = Maybe.None();

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

  $: inputLanguage = bilingual ?
      selectedLanguage :
      Maybe.orSome(selectedLanguage, R.map(et.kielisyysKey, energiatodistusKieli));

  function toggleLanguageSelection() {
    if (bilingual) {
      selectedLanguage = R.equals(selectedLanguage, 'fi') ? 'sv' : 'fi';
    }
  }

  $: pdfUrl = Maybe.map(
    i => api.url.pdf(version, i, inputLanguage),
    id
  );

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
  }

  const noop = () => {};

  $: persistentDraft = id.isSome() && R.propEq('tila-id', et.tila.draft, energiatodistus)
</script>

<style type="text/postcss">
  button {
    @apply w-32 border-2 border-secondary flex flex-col justify-center items-center;
  }

  button:not(:first-child) {
    @apply h-24;
  }

  .description {
    @apply font-bold;
  }

  button:not(:last-child) {
    @apply border-b-0;
  }
</style>

{#if signingActive} <Signing {energiatodistus} reload={cancel} /> {/if}

<div class="flex flex-col text-secondary">
  <button on:click={toggleLanguageSelection}>
  {#if bilingual}
    <div class="flex flex-row w-full">
      {#each ['fi', 'sv'] as language}
        <div class="w-1/2 text-light description"
             class:bg-primary={R.equals(selectedLanguage, language)}
             class:bg-secondary={!R.equals(selectedLanguage, language)}>
          {language}
        </div>
      {/each}
    </div>
  {:else}
    <div class="w-full text-light description bg-primary">{energiatodistusKieli.map(et.kielisyysKey).some()}</div>
  {/if}
  </button>
  <button on:click={save(noop)}>
    <span class="description">
      {id.isSome() ? $_('energiatodistus.toolbar.save') : $_('energiatodistus.toolbar.new')}
    </span>
    <span class="text-2xl font-icon">save</span>
  </button>
  <button on:click={cancel}>
    <span class="description">Peruuta muutokset</span>
    <span class="text-2xl font-icon">undo</span>
  </button>
  {#if persistentDraft}
  <button on:click={saveComplete(openSigning)}>
    <div class="description">Allekirjoita</div>
    <span class="text-2xl font-icon border-b-3 border-secondary">
      create
    </span>
  </button>
  {/if}
  {#if id.isSome()}
    <button>
      <span class="description">Kopioi pohjaksi</span>
      <span class="text-2xl font-icon">file_copy</span>
    </button>
  {/if}
  {#each pdfUrl.toArray() as url}
    <button on:click={save(() => openUrl(url))}>
      <span class="block description">Esikatselu</span>
      <span class="text-2xl font-icon">picture_as_pdf</span>
    </button>
  {/each}
  {#if persistentDraft}
    <Confirm
      let:confirm
      confirmButtonLabel={$_('confirm.button.delete')}
      confirmMessage={$_('confirm.you-want-to-delete')}>
      <button on:click={() => confirm(deleteEnergiatodistus)}>
        <span class="description">Poista</span>
        <span class="text-2xl font-icon">delete_forever</span>
      </button>
    </Confirm>
  {/if}
  {#each eTehokkuus.toArray() as e}
    <div class="border-2 border-dark py-2 bg-secondary">
      <div class="font-bold text-center text-sm text-light pb-1">
        {$_('energiatodistus.tulokset.e-luku')} {e['e-luku']}
      </div>
      <div class="font-bold text-center text-sm text-light pt-1">
        {$_('energiatodistus.tulokset.e-luokka')}
        {e['e-luokka']}<sub>{energiatodistus.versio}</sub>
      </div>
    </div>
  {/each}
</div>
