<script>
  import * as R from 'ramda';
  import * as Viestit from '@Component/viesti/viesti-util';

  import { _, locale } from '@Language/i18n';

  export let user;
  export let whoami;
  export let icons = false;

  $: formatSender = Viestit.formatSender($_);
</script>

<style>
  .from-me {
    @apply text-primary font-bold;
  }
</style>

{#if icons}<span class="font-icon">person</span>{/if}
{#if R.eqProps('id', user, whoami)}
  <span class="from-me">
    {$_('viesti.ketju.existing.self')}
  </span>
{:else}
  <span>
    {formatSender(user)}
  </span>
{/if}
