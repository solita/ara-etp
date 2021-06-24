<script>
  import * as Maybe from '@Utility/maybe-utils';

  export let href;
  export let text = '';
  export let target = '_self';
  export let icon = Maybe.None();
  export let disabled = false;
  export let bold = false;
</script>

<style type="text/postcss">
  a {
    @apply relative inline-flex items-start text-link border-b-1 border-transparent font-semibold;
  }

  a:hover {
    @apply cursor-pointer border-primary;
  }
  a.disabled {
    @apply pointer-events-none text-disabled;
  }
</style>

<!-- purgecss: disabled font-bold -->

<a {href} {target} class:disabled class:font-bold={bold}>
  {#each Maybe.toArray(icon) as i}
    <span class="font-icon mr-1">{i}</span>
  {/each}
  {#if text}
    <span>{text}</span>
  {/if}
</a>
