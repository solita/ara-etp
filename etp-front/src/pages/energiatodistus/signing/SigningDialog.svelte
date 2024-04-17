<script>
  import CardSigning from './CardSigning.svelte';

  export let energiatodistus;
  export let reload;
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }
</style>

<dialog on:click|stopPropagation>
  <CardSigning {energiatodistus} {reload} />
</dialog>
