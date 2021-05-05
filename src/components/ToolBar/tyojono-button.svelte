<script>
  import Spinner from '@Component/Spinner/Spinner';

  export let valvonta;
  export let disabled;
</script>

<style>
  .tyojono-active {
    @apply text-primary;
  }
</style>

{#if !disabled}
<button {disabled} class="hover:text-primary min-h-3em" class:tyojono-active={valvonta.active} on:click>
  <span class="font-icon text-2xl">
    {#if !valvonta.active}
      star_border
    {:else}
      star
    {/if}
  </span>
</button>
{:else}
<div class="flex justify-center items-center">
  <Spinner />
</div>
{/if}