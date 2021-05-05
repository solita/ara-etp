<script>
  import Spinner from '@Component/Spinner/Spinner';

  export let valvonta;
  export let disabled;
</script>


<!-- purgecss: text-primary text-darkgrey -->
{#if !disabled}
<button disabled={valvonta.ongoing} class="min-h-3em" class:hover:text-primary={!valvonta.ongoing} class:text-primary={valvonta.pending && !valvonta.ongoing} class:text-darkgrey={valvonta.ongoing} class:hover:text-darkgrey={valvonta.ongoing} on:click>
  <span class="font-icon text-2xl">
    {#if !valvonta.pending}
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