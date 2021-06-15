<script>
  import { onMount } from 'svelte';
  import TextButton from '@Component/Button/TextButton';

  export let truncate = true;

  let node;
  let truncated;

  onMount(() => {
    truncated =
      !!node &&
      !!node.children &&
      !!node.children[0] &&
      node.children[0].offsetWidth < node.children[0].scrollWidth;
  });
</script>

<style>
  div.truncate-children :global(> *) {
    @apply truncate;
  }
</style>

<svelte:window
  on:resize={() =>
    (truncated =
      !!node &&
      !!node.children &&
      !!node.children[0] &&
      node.children[0].offsetWidth < node.children[0].scrollWidth)} />

<div bind:this={node} class="min-w-0" class:truncate-children={truncate}>
  <slot />
</div>

{#if truncated}
  <TextButton
    on:click={_ => (truncate = !truncate)}
    type={'button'}
    icon={truncate ? 'expand_more' : 'expand_less'}
    text={truncate ? 'Näytä lisää' : 'Näytä vähemmän'} />
{/if}
