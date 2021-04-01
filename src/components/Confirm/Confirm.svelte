<script>
  import Button from '@Component/Button/Button';
  import { _ } from '@Language/i18n';

  export let confirmMessage;
  export let confirmButtonLabel;

  let hide = true;
  let resolve = () => {};
  let reject = () => {};

  const confirmAction = (action, ...params) => {
    hide = false;

    resolve = () => {
      hide = true;
      action(...params);
    };
    reject = () => {
      hide = true;
    };
  };
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }
  .content {
    @apply relative bg-light w-2/3 py-32 border-dark border-1 flex flex-col justify-center;
  }

  .close {
    @apply absolute top-0 right-0 pt-4 pr-4 text-primary cursor-pointer;
  }

  .message {
    @apply text-primary text-center font-bold px-24;
  }
  .buttons {
    @apply flex justify-center items-center mt-8;
  }
</style>

<slot confirm={confirmAction} />

{#if !hide}
  <dialog open={!hide} on:click|stopPropagation>
    <div class="content">
      <span class="material-icons close" on:click|stopPropagation={reject}>
        highlight_off
      </span>
      <p class="message">{confirmMessage}</p>
      <div class="buttons">
        <div class="px-2">
          <Button
            prefix="confirm-submit"
            on:click={resolve}
            style="primary"
            text={confirmButtonLabel} />
        </div>
        <div class="px-2">
          <Button
            prefix="confirm-reject"
            on:click={reject}
            style="secondary"
            text={$_('peruuta')} />
        </div>
      </div>
    </div>
  </dialog>
{/if}
