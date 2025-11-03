<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';

  import User from './user.svelte';

  export let sender;
  export let whoami;
  export let recipients = [];
  export let recipientGroup = Maybe.None();
  export let icons = false;
</script>

<style>
  .to .separator:not(:last-child)::after {
    content: ',';
  }
</style>

<div class="flex">
  <User {icons} {whoami} user={sender} />
  <span class="mx-2 font-icon">arrow_right_alt</span>
  <div class="to flex truncate">
    {#each Maybe.toArray(recipientGroup) as group}
      {#if icons}<span class="font-icon">group</span>{/if}
      <span>
        {Locales.label($locale, group)}
      </span> <span class="separator" />
    {/each}
    {#each recipients as recipient}
      <User {icons} {whoami} user={recipient} /> <span class="separator" />
    {/each}
  </div>
</div>
