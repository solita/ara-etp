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
    @apply relative flex py-4 px-6 font-bold justify-center border-dark border-b-3 cursor-pointer uppercase shadow-none tracking-xl outline-none;
    transition: box-shadow 0.1s ease-in-out;
  }

  a:hover,
  a.active {
    @apply shadow-hover-2-primary border-hover;
  }

  a:focus {
    @apply shadow-hover-2-secondary border-secondary;
  }

  a:hover {
    @apply bg-background;
  }

  .badge {
    @apply inline-flex justify-center items-center absolute rounded-full bg-primary text-light font-medium text-xs;
    top: -1.5em;
    right: -1em;
    padding-left: 0.6em;
    padding-right: 0.4em;
    height: 1.75em;
  }
</style>

<!-- purgecss: active -->
{#if disabled}
  <span class="disabled">{label}</span>
{:else}
  <a
    {href}
    class:active={R.compose(
      R.equals($location),
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
