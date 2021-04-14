<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Viestit from '@Component/viesti/viesti-util';

  import { _, locale } from '@Language/i18n';

  export let sender;
  export let whoami;
  export let recipients = [];
  export let recipientGroup = Maybe.None();

  $: formatSender = Viestit.formatSender($_);
</script>

<style>
  .from-me {
    @apply text-primary;
  }
  .to span:not(:last-child)::after {
    content: ', ';
  }
</style>

<div class="flex ">
  {#if R.eqProps('id', sender, whoami)}
    <strong class="from-me whitespace-no-wrap">
      {$_('viesti.ketju.existing.self')}
    </strong>
  {:else}
    <span class="whitespace-no-wrap">{formatSender(sender)}</span>
  {/if}

  {#if !R.isEmpty(recipients) || Maybe.isSome(recipientGroup)}
    <span class="material-icons mx-2"> arrow_right_alt </span>
  {/if}

  <div class="to truncate">
    {#each Maybe.toArray(recipientGroup) as group}
      <span> {Locales.label($locale, group)} </span>
    {/each}
    {#each recipients as recipient}
      <span>
        {formatSender(recipient)}
      </span>
    {/each}
  </div>
</div>
