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
    @apply fixed left-0 top-0 z-50 flex h-screen w-screen cursor-default items-center justify-center bg-hr;
  }
  .content {
    @apply relative flex w-2/3 flex-col justify-center rounded-md bg-light px-10 py-10 shadow-lg;
  }

  .close {
    @apply absolute right-0 top-0 cursor-pointer pr-4 pt-4 text-primary;
  }

  .message {
    @apply px-24 text-center font-bold text-primary;
  }
  .buttons {
    @apply mt-8 flex items-center justify-center;
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
