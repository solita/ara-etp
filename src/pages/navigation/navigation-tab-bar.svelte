<script>
  import * as R from 'ramda';
  import NavigationTab from './navigation-tab';
  import BadgeProvider from './badge-provider';
  import * as Navigation from '@Utility/navigation';

  export let idTranslate;
  export let location;
  export let whoami;
  export let i18n;
  export let config;

  $: links = Navigation.navigationParse(
    config.isDev,
    i18n,
    whoami,
    location,
    idTranslate
  );
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

{#if !R.isEmpty(links)}
  <div>
    {#each links as link}
      {#if link.badge}
        <BadgeProvider badgeFuture={link.badge} let:badge>
          <NavigationTab
            label={link.label}
            href={link.href}
            activePath={link.activePath}
            disabled={link.disabled}
            {badge} />
        </BadgeProvider>
      {:else}
        <NavigationTab
          label={link.label}
          href={link.href}
          activePath={link.activePath}
          disabled={link.disabled} />
      {/if}
    {/each}
  </div>
{/if}
