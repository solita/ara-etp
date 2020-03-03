<script>
  import * as R from 'ramda';

  export let actions;
  export let index;
</script>

<style type="text/postcss">
  span {
    @apply cursor-pointer;
  }
</style>

{#each actions as action}
  <div>
    {#if R.equals(action.type, 'remove')}
      <span class="font-icon" on:click={e => action.update(index)}>
        remove_circle_outline
      </span>
    {/if}
  </div>
{/each}
