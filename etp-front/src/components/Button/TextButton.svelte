<script>
  export let text = '';
  export let icon = '';
  export let type = 'submit';
  export let title = '';
  export let disabled = false;
  export let largeIcon = false;
  export let dataCy = undefined;
</script>

<style type="text/postcss">
  button {
    @apply flex items-center justify-start border-b-1 border-transparent font-semibold text-primary;
  }

  button:hover:not(:disabled) {
    @apply border-primary;
  }

  button:active {
    @apply text-secondary;
  }

  button:focus {
    @apply outline-none;
  }

  button:disabled {
    @apply cursor-not-allowed text-disabled;
  }
</style>

<button {type} {title} {disabled} data-cy={dataCy} on:click>
  <span class="icon-outlined mr-1 font-icon {largeIcon ? 'text-3xl' : ''}"
    >{icon}</span>
  {text}
</button>
