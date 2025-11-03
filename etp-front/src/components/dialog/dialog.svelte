<script>
  import Button from '@Component/Button/Button';
  import Error from '@Component/Error/Error';

  export let header;
  export let error;
  export let buttons;
  export let form;
</script>

<style type="text/postcss">
  dialog {
    @apply fixed left-0 top-0 z-50 flex h-screen w-screen cursor-default justify-center overflow-y-auto bg-hr;
    align-items: safe center;
  }

  .content {
    @apply relative flex w-2/3 flex-col justify-center rounded-md bg-light px-10 py-10 shadow-lg;
  }

  h1 {
    @apply mb-4 border-b-1 border-tertiary pb-2 text-lg font-bold uppercase tracking-xl text-secondary;
  }

  .buttons {
    @apply mt-5 flex flex-wrap items-center border-t-1 border-tertiary pt-5;
  }
</style>

<dialog on:click|stopPropagation>
  <form class="content" bind:this={form} on:submit>
    {#if header}
      <h1>{header}</h1>
    {/if}
    {#each error.toArray() as txt}
      <Error text={txt} />
    {/each}
    <slot />
    <div
      class="buttons flex-col space-y-2 lg:flex-row lg:space-x-2 lg:space-y-0">
      {#each buttons as button}
        <Button {...button} on:click={button['on:click']} />
      {/each}
    </div>
  </form>
</dialog>
