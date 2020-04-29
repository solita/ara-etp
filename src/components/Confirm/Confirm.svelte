<script>
  import Button from '@Component/Button/Button';

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
  div {
    @apply flex justify-center items-center fixed top-0 bottom-0 left-0 right-0 bg-light opacity-75;
  }
</style>

<slot confirm={confirmAction} />

{#if !hide}
  <dialog open={!hide}>
    <div>
      <Button on:click={resolve} style="primary" text="Hyväksy" />
      <Button on:click={reject} style="secondary" text="Peru" />
    </div>
  </dialog>
{/if}
