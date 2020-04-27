<script>
  import LanguageSelect from './LanguageSelect';
  import * as Maybe from "@Utility/maybe-utils";
  export let version;
  export let id = Maybe.None();

  const pdfUrl = Maybe.map(
    i => `/api/private/energiatodistukset/${version}/${i}/pdf`,
    id);

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
  <button on:click = {save}>
    <span class="description">Tallenna</span>
    <span class="text-2xl font-icon">save</span>
  </button>
  <button on:click = {cancel}>
    <span class="description">Peruuta muutokset</span>
    <span class="text-2xl font-icon">undo</span>
  </button>
  <button>
    <span class="description">Allekirjoita</span>
    <span class="text-2xl font-icon border-b-3 border-secondary">create</span>
  </button>
  <button>
    <span class="description">Kopioi pohjaksi</span>
    <span class="text-2xl font-icon">file_copy</span>
  </button>
  {#if pdfUrl.isSome()}
  <button>
    <a href={pdfUrl.some()}>
      <span class="description">Tulosta PDF</span>
      <span class="text-2xl font-icon">print</span>
    </a>
  </button>
  {/if}
  <button>
    <span class="description">Poista</span>
    <span class="text-2xl font-icon">delete_forever</span>
  </button>
</div>
