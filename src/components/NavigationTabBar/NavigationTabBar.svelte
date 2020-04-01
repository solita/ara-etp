<script>
  import NavigationTab from '@Component/NavigationTab/NavigationTab';

  export let links;
</script>

<style type="text/postcss">
  div {
    @apply flex pb-4;
  }

  div:last-child::after {
    content: '';
    @apply flex-grow border-dark border-b-3;
  }
</style>

<div>
  {#each links as link}
    <NavigationTab text={link.text} href={link.href} />
  {/each}
</div>
