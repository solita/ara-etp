<script>
  import Button from '@Component/Button/Button.svelte';
  import { _ } from '@Language/i18n';

  export let reload;

  const i18n = $_;

  const sign = () => {};
</script>

<style type="text/postcss">
  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<div>
  <div>Allekirjoitamme ilman korttia kiitos</div>
  <div class="buttons">
    <div class="mr-10 mt-5">
      <Button
        prefix="signing-submit"
        text={i18n('energiatodistus.signing.button.start')}
        on:click={sign} />
    </div>
    <div class="mt-5">
      <Button
        prefix="signing-cancel"
        text={i18n('energiatodistus.signing.button.close')}
        style={'secondary'}
        on:click={reload} />
    </div>
  </div>
</div>
