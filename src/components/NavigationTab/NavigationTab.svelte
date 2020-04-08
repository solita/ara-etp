<script>
  import active from 'svelte-spa-router/active';
  import { link } from 'svelte-spa-router';
  import { _ } from '@Language/i18n';
  export let text;
  export let href;
  export let activePath;
</script>

<style type="text/postcss">
  a {
    @apply flex py-4 px-6 font-bold justify-center border-dark border-b-3 cursor-pointer uppercase shadow-none;
    transition: box-shadow 0.1s ease-in-out;
    letter-spacing: 0.15em; /* TODO: Generate in tailwind */
  }

  a:focus,
  a:hover {
    @apply shadow-hover-2-primary border-hover;
  }

  a:hover {
    @apply bg-background;
  }

  a:focus {
    @apply outline-none;
  }

  a:active {
    @apply text-hover border-secondary shadow-hover-2-secondary;
  }

  :global(a.active) {
    @apply shadow-hover-2-secondary border-secondary;
  }
</style>

<a
  {href}
  use:link
  use:active={activePath || href}
  class="navigationtab"
  tabindex="0">
  {$_(text)}
</a>
