<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import { link, location } from 'svelte-spa-router';
  import { _ } from '@Language/i18n';

  export let label;
  export let href;
  export let activePath;
  export let disabled = false;
  export let badge = Maybe.None();
</script>

<style type="text/postcss">
  a,
  .disabled {
    @apply relative flex cursor-pointer justify-center border-b-3 border-dark py-4 font-bold uppercase tracking-xl shadow-none outline-none;
    transition: box-shadow 0.1s ease-in-out;
  }

  a:hover,
  a.active {
    @apply border-hover shadow-hover-2-primary;
  }

  a:focus {
    @apply border-secondary shadow-hover-2-secondary;
  }

  a:hover {
    @apply bg-background;
  }

  .badge {
    @apply absolute inline-flex items-center justify-center rounded-full bg-primary text-xs font-medium text-light;
    top: -1.5em;
    right: -1em;
    padding-left: 0.6em;
    padding-right: 0.4em;
    height: 1.75em;
    z-index: 1;
  }
</style>

{#if disabled}
  <span class="disabled px-2 lg:px-6">{label}</span>
{:else}
  <a
    {href}
    data-cy={label}
    class="px-2 lg:px-6"
    class:active={R.compose(
      R.equals($location),
      R.head,
      R.split('?'),
      R.dropWhile(R.equals('#')),
      R.defaultTo(href)
    )(activePath)}>
    <span class="relative">
      {label}
      {#each Maybe.toArray(badge) as b}
        <span class="badge">{b}</span>
      {/each}
    </span>
  </a>
{/if}
