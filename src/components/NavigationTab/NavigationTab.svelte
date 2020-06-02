<script>
  import * as R from 'ramda';
  import active from 'svelte-spa-router/active';
  import { link } from 'svelte-spa-router';
  import { _ } from '@Language/i18n';
  export let label;
  export let href;
  export let activePath;
</script>

<style type="text/postcss">
  a {
    @apply flex py-4 px-6 font-bold justify-center border-dark border-b-3 cursor-pointer uppercase shadow-none tracking-xl outline-none;
    transition: box-shadow 0.1s ease-in-out;
  }

  a:hover,
  :global(a.active) {
    @apply shadow-hover-2-primary border-hover;
  }

  a:hover {
    @apply bg-background;
  }
</style>

<a
  {href}
  use:active={R.compose( R.dropWhile(R.equals('#')), R.defaultTo(href) )(activePath)}
  class="navigationtab"
  tabindex="0">
  {label}
</a>
