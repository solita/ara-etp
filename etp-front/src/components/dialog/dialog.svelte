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
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center overflow-y-auto;
    align-items: safe center;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 pt-5 border-t-1 border-tertiary;
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
      class="buttons flex-col lg:flex-row space-y-2 lg:space-x-2 lg:space-y-0">
      {#each buttons as button}
        <Button {...button} on:click={button['on:click']} />
      {/each}
    </div>
  </form>
</dialog>
