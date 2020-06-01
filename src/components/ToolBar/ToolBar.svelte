<script>
  import { replace } from 'svelte-spa-router';
  import LanguageSelect from './LanguageSelect';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import Confirm from '@Component/Confirm/Confirm';
  import * as api from '@Component/Energiatodistus/energiatodistus-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  export let version;
  export let id = Maybe.None();

  const pdfUrl = Maybe.map(
    i => `/api/private/energiatodistukset/${version}/${i}/pdf`,
    id
  );

  const signUrl = Maybe.map(
    i => `#/energiatodistus/${version}/${i}/allekirjoitus`,
    id
  );

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
  export let cancel = _ => {};
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

<div class="flex flex-col text-secondary">
  <button>
    <LanguageSelect />
  </button>
  <button on:click={save}>
    <span class="description">
      {id.isSome() ? $_('energiatodistus.toolbar.save') : $_('energiatodistus.toolbar.new')}
    </span>
    <span class="text-2xl font-icon">save</span>
  </button>
  <button on:click={cancel}>
    <span class="description">Peruuta muutokset</span>
    <span class="text-2xl font-icon">undo</span>
  </button>
  {#if signUrl.isSome()}
    <button>
      <a href={signUrl.some()}>
        <div class="description">Allekirjoita</div>
        <span class="text-2xl font-icon border-b-3 border-secondary">
          create
        </span>
      </a>
    </button>
  {/if}
  {#if id.isSome()}
    <button>
      <span class="description">Kopioi pohjaksi</span>
      <span class="text-2xl font-icon">file_copy</span>
    </button>
  {/if}
  {#if pdfUrl.isSome()}
    <button>
      <a href={pdfUrl.some()}>
        <span class="description">Tulosta PDF</span>
        <span class="text-2xl font-icon">print</span>
      </a>
    </button>
  {/if}
  {#if id.isSome()}
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
</div>
