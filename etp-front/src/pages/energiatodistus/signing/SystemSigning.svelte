<script>
  import Button from '@Component/Button/Button.svelte';
  import { _ } from '@Language/i18n';
  import Spinner from '@Component/Spinner/Spinner.svelte';

  export let reload;

  const i18n = $_;

  let inProgress = false;

  const sign = () => {
    inProgress = true;
  };
</script>

<style type="text/postcss">
  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<div>
  <div>Allekirjoitamme ilman korttia kiitos</div>

  {#if inProgress}
    <div class="mt-2">
      <Spinner />
    </div>
  {/if}

  <div class="buttons">
    <div class="mr-10 mt-5">
      <Button
        prefix="signing-submit"
        disabled={inProgress}
        text={i18n('energiatodistus.signing.button.start')}
        on:click={sign} />
    </div>
    <div class="mt-5">
      <Button
        prefix="signing-cancel"
        disabled={inProgress}
        text={i18n('energiatodistus.signing.button.close')}
        style={'secondary'}
        on:click={reload} />
    </div>
  </div>
</div>
