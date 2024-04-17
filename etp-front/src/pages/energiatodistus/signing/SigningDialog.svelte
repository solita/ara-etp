<script>
  import { _ } from '@Language/i18n';

  import CardSigning from './CardSigning.svelte';

  export let energiatodistus;
  export let reload;

  export let selection = 'card';

  const i18n = $_;

  const isSigningMethodCard = selectedMethod => selectedMethod === 'card';
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl;
  }
</style>

<dialog on:click|stopPropagation>
  <div class="content">
    <h1>{i18n('energiatodistus.signing.header')}</h1>

    <div>
      {#if isSigningMethodCard(selection)}
        <CardSigning {energiatodistus} {reload} />
      {/if}
    </div>
  </div>
</dialog>
