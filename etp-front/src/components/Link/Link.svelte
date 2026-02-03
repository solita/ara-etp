<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as R from 'ramda';

  export let href;
  export let text = '';
  export let dataCy;
  export let target = '_self';
  export let icon = Maybe.None();
  export let disabled = false;
  export let bold = false;
  export let additionalClasses = '';
</script>

<style type="text/postcss">
  a {
    @apply relative inline-flex items-start border-b-1 border-transparent font-semibold text-link;
  }

  a:hover {
    @apply cursor-pointer border-primary;
  }
  a.disabled {
    @apply pointer-events-none text-disabled;
  }
</style>

<a
  {href}
  {target}
  class:disabled
  class:font-bold={bold}
  data-cy={R.defaultTo(text, dataCy)}
  on:click>
  {#each Maybe.toArray(icon) as i}
    <span class="mr-1 font-icon">{i}</span>
  {/each}
  {#if text}
    <span class={additionalClasses}>{text}</span>
  {/if}
</a>
