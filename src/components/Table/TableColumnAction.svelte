<script>
  import * as R from 'ramda';

  export let actions;
  export let index;
</script>

<style type="text/postcss">
  span {
    @apply cursor-pointer;
  }

  .action {
    @apply cursor-pointer;
  }

  .action:hover {
    @apply underline;
  }
</style>

{#each actions as action}
  <div>
    {#if R.equals(action.type, 'remove')}
      <span class="font-icon" on:click={e => action.update(index)}>
        remove_circle_outline
      </span>
    {:else}
      <div class="action" on:click={e => action.update(index)}>
        <span class="font-icon self-center">{action.icon}</span>
        <span class="self-center">{action.text}</span>
      </div>
    {/if}
  </div>
{/each}
