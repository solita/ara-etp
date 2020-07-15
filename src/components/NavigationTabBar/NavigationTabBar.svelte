<script>
  import NavigationTab from '@Component/NavigationTab/NavigationTab';
  import * as Navigation from '@Utility/navigation';

  export let location;
  export let user;
  export let i18n;

  $: links = Navigation.navigationParse(i18n, user, location);
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
    <NavigationTab
      label={link.label}
      href={link.href}
      activePath={link.activePath}
      disabled={link.disabled} />
  {/each}
</div>
