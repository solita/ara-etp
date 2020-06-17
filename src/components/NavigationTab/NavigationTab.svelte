<script>
  import * as R from 'ramda';
  import { link, location } from 'svelte-spa-router';
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
  a.active {
    @apply shadow-hover-2-primary border-hover;
  }

  a:hover {
    @apply bg-background;
  }
</style>

<a
  {href}
  class:active={R.compose( R.equals($location), R.dropWhile(R.equals('#')), R.defaultTo(href) )(activePath)}
  class="navigationtab"
  tabindex="0">
  {label}
</a>
