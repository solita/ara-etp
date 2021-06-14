<script>
  import * as R from 'ramda';
  import * as Viestit from '@Component/viesti/viesti-util';

  import { _ } from '@Language/i18n';

  export let user;
  export let whoami;
  export let icons = false;

  $: formatSender = Viestit.formatSender($_);
</script>

<style>
</style>

{#if icons}<span class="font-icon">person</span>{/if}
{#if R.eqProps('id', user, whoami)}
  <span class="text-primary font-bold">
    {$_('viesti.ketju.existing.self')}
  </span>
{:else}
  <span>
    {formatSender(user)}
  </span>
{/if}
