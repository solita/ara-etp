<script>
  import * as R from 'ramda';

  import Link from '@Component/Link/Link';

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

<div on:click|stopPropagation>
  {#each actions as action}
    {#if R.equals(action.type, 'remove')}
      <span class="font-icon" on:click={e => action.update(index)}>
        remove_circle_outline
      </span>
    {:else if R.equals(action.type, 'link')}
      <Link href={action.href} text={action.text} />
    {:else}
      <div class="action" on:click={e => action.update(index)}>
        <span class="font-icon self-center">{action.icon}</span>
        <span class="self-center">{action.text}</span>
      </div>
    {/if}
  {/each}
</div>
