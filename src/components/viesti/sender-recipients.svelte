<script>
  import { _ } from '@Language/i18n';

  export let sender;
  export let senderIsSelf = false;
  export let recipients = [];
  export let recipientGroup;
</script>

<style>
  .from-to-container.self .from-me {
    @apply text-primary block;
  }
  .from-to-container:not(.self) .from {
    @apply block;
  }
  .from-to-container .to span:not(:last-child)::after {
    content: ', ';
  }
</style>

<div class="flex from-to-container" class:self={senderIsSelf}>
  <span class="from hidden">{sender}</span>
  <strong class="from-me hidden">{$_('viesti.ketju.existing.self')}</strong>

  <span class="material-icons mx-2"> arrow_right_alt </span>
  <div class="to">
    <span> {recipientGroup} </span>
    {#each recipients as recipient}
      <span>
        {`${recipient.etunimi} ${recipient.sukunimi}`}
      </span>
    {/each}
  </div>
</div>
