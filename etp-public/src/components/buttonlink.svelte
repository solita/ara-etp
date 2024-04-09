<script context="module">
  export const styles = {
    green: { text: 'text-white', bg: 'bg-green', focus: 'bg-darkgreen' },
    white: { text: 'text-ashblue', bg: 'bg-white', focus: 'bg-grey' },
    ashblue: { text: 'text-white', bg: 'bg-ashblue', focus: 'bg-darkashblue' }
  };
</script>

<script>
  export let text;
  export let bg;
  export let focus;
  export let href;
  export let target;
</script>

<style>
  a:focus,
  a:active,
  a:hover {
    box-shadow:
      inset 0 0 0 3px #fff,
      0 0 0 1px #979797;
  }
  a.bg-white:focus:not(:disabled),
  a.bg-white:active:not(:disabled),
  a.bg-white:hover:not(:disabled) {
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
    a {
      display: none;
    }
  }
</style>

<a
  {href}
  {target}
  class={`inline-flex m-1 uppercase text-sm font-bold tracking-wider rounded-full px-8 py-3 focus:outline-none ${text} ${bg} hover:${focus} focus:${focus} space-x-3 items-center`}>
  <slot />
</a>
