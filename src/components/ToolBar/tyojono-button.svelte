<script>
  export let valvonta;
  export let disabled;
</script>

<!-- purgecss: hover:text-light hover:bg-primary text-primary text-darkgrey hover:text-darkgrey -->
<button
  disabled={valvonta.ongoing || disabled}
  class="min-h-3em"
  class:hover:text-light={!valvonta.ongoing}
  class:hover:bg-primary={!valvonta.ongoing}
  class:text-primary={valvonta.pending && !valvonta.ongoing}
  class:text-darkgrey={valvonta.ongoing}
  class:hover:text-darkgrey={valvonta.ongoing}
  on:click>
  <span class="font-icon text-2xl">
    {#if !valvonta.pending}
      star_border
    {:else}
      star
    {/if}
  </span>
</button>
