<script context="module">
  export const styles = {
    green: { text: 'text-white', bg: 'bg-green', focus: 'bg-darkgreen' },
    white: { text: 'text-ashblue', bg: 'bg-white', focus: 'bg-grey' },
    ashblue: { text: 'text-white', bg: 'bg-ashblue', focus: 'bg-darkashblue' }
  };
</script>

<script>
  export let text;
  export let ariaLabel = '';

  export let bg;
  export let focus;
  export let type = 'submit';

  export let disabled = false;

  export let dataCy;
</script>

<style>
  button:focus:not(:disabled),
  button:active:not(:disabled),
  button:hover:not(:disabled) {
    box-shadow:
      inset 0 0 0 3px #fff,
      0 0 0 1px #979797;
  }

  button.bg-white:focus:not(:disabled),
  button.bg-white:active:not(:disabled),
  button.bg-white:hover:not(:disabled) {
    box-shadow:
      inset 0 0 0 3px #2e5053,
      0 0 0 1px #fff;
  }

  .bg-ashblue:hover,
  .bg-ashblue:focus {
    @apply bg-darkashblue;
  }

  .bg-green:hover,
  .bg-green:focus {
    @apply bg-darkgreen;
  }

  @media print {
    button {
      display: none;
    }
  }
</style>

<button
  aria-label={ariaLabel}
  on:click
  {disabled}
  {type}
  data-cy={dataCy}
  class:bg-lightgrey={disabled}
  class:cursor-not-allowed={disabled}
  class:shadow-none={disabled}
  class={`inline-flex space-x-3 items-center m-1 uppercase text-sm font-bold tracking-wider rounded-full px-8 py-3 focus:outline-none ${text} ${bg} hover:${
    disabled ? 'bg-lightgrey' : focus
  } focus:${disabled ? 'bg-lightgrey' : focus} border-transparent`}>
  <slot />
</button>
