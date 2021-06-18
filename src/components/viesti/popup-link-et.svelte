<script>
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import H1 from '@Component/H/H1';
  import { _ } from '@Language/i18n';

  export let title;
  export let inputLabel;

  export let primaryAction;
  export let primaryButtonLabel;

  export let secondaryAction;
  export let secondaryButtonLabel;

  const i18n = $_;

  let inputValue;
  let hide = true;
  let resolvePrimary = () => {};
  let resolveSecondary = () => {};
  let reject = () => {};

  const popupAction = () => {
    hide = false;

    resolvePrimary = () => {
      hide = true;
      primaryAction(inputValue);
    };
    resolveSecondary = () => {
      hide = true;
      secondaryAction(inputValue);
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
    @apply relative bg-light w-2/3 py-12 px-8 border-dark border-1 rounded flex flex-col justify-center;
  }

  .close {
    @apply absolute top-0 right-0 pt-4 pr-4 text-primary cursor-pointer;
  }
</style>

<slot open={popupAction} />

{#if !hide}
  <dialog open={!hide} on:click|stopPropagation>
    <div class="content">
      <span class="material-icons close" on:click|stopPropagation={reject}>
        highlight_off
      </span>

      <H1 text={title} />
      <div class="mr-64">
        <Input
          id={'popup.value'}
          name={'popup.value'}
          label={inputLabel}
          compact={!inputLabel}
          bind:model={inputValue}
          {i18n} />
      </div>
      <div class="flex justify-start items-center mt-8">
        <div class="mr-2">
          <Button
            prefix="popup-submit"
            on:click={resolvePrimary}
            style="primary"
            text={primaryButtonLabel} />
        </div>
        <div class="mr-2">
          <Button
            prefix="popup-unlink"
            on:click={resolveSecondary}
            style="secondary"
            text={secondaryButtonLabel} />
        </div>
        <div class="justify-self-end ml-auto">
          <Button
            prefix="popup-reject"
            on:click={reject}
            style="secondary"
            text={i18n('peruuta')} />
        </div>
      </div>
    </div>
  </dialog>
{/if}
