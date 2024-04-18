<script>
  import Button from '@Component/Button/Button.svelte';
  import { _ } from '@Language/i18n';

  export let reload;

  const i18n = $_;
</script>

<style type="text/postcss">
  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<div>
  <div>Allekirjoitamme ilman korttia kiitos</div>
  <div class="buttons">
    <div class="mt-5">
      <Button
        text={i18n('energiatodistus.signing.button.close')}
        style={'secondary'}
        on:click={reload} />
    </div>
  </div>
</div>
