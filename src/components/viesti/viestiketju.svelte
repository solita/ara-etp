<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';

  import * as Viestit from './viesti-util';

  import { _ } from '@Language/i18n';

  export let ketju;
  export let whoami;

  const sentTime = R.compose(R.prop('sent-time'), R.last, R.prop('viestit'));

  $: participants = R.compose(
    R.reduce(R.mergeLeft, {}),
    R.map(viesti => {
      if (Viestit.isSelfSent(viesti, whoami)) {
        return { [viesti.from.id]: $_('viesti.mina') };
      }
      return { [viesti.from.id]: Viestit.formatSender($_, viesti.from) };
    }),
    R.prop('viestit')
  )(ketju);

  $: currentUserPartOfKetju = R.compose(
    R.has(R.__, participants),
    R.prop('id')
  )(whoami);
</script>

<style>
  a:first-child {
    @apply border-t-2;
  }
  .participants span:not(:last-child)::after {
    content: ', ';
  }
  .expanding-subject .subject {
    transition: all 0.3s;
    @apply rounded border;
  }
  .expanding-subject .subject:not(:hover) {
    @apply truncate border-transparent;
  }
  .expanding-subject .subject:hover {
    @apply bg-light border-background whitespace-pre-wrap absolute z-10;
  }
  .expanding-subject .message {
    @apply hidden;
  }
</style>

<!-- purgecss: font-bold text-primary -->

<a
  href={`#/viesti/${ketju.id}`}
  class="flex flex-col border-b-2 border-background hover:bg-background p-2">
  <div class="flex items-start">
    <span class="py-1 w-2/12 border border-transparent">
      {R.compose(Formats.formatDateInstant, sentTime)(ketju)}
    </span>
    <div class="py-1 w-3/12 flex font-bold">
      <span
        class:text-primary={R.propEq(
          'id',
          R.path(['from', 'id'], R.last(ketju.viestit)),
          whoami
        )}>
        {participants[R.path(['from', 'id'], R.last(ketju.viestit))]}
      </span>
    </div>
    <div
      class="flex w-7/12 justify-start whitespace-no-wrap space-x-1"
      class:expanding-subject={R.gt(R.length(R.prop('subject', ketju)), 55)}>
      <span class="p-1 subject font-bold self-start">
        {ketju.subject}
      </span>

      <span class="message truncate flex-shrink text-sm self-center">
        {R.last(ketju.viestit).body}
      </span>
    </div>
  </div>

  <div class="flex items-center">
    <div class="flex flex-col w-2/12">
      <span class="block">
        {R.compose(Formats.formatHoursMinutes, sentTime)(ketju)}
      </span>
    </div>

    <div class="flex w-10/12 items-center space-x-2">
      <div class="flex items-center flex-shrink">
        <span class="font-icon outline mr-1">chat_bubble_outline</span>
        <span>{R.length(ketju.viestit)}</span>

        {#if R.gt(R.length(ketju.viestit), 1)}
          <span class="ml-1">{$_('viesti.messages')}</span>
        {:else}
          <span class="ml-1">{$_('viesti.message')}</span>
        {/if}
      </div>
      {#if R.gt(R.length(R.values(participants)), 1)}
        <div class="flex items-center w-full flex-grow">
          <span class="font-icon">people</span>
          <span class="mx-1">
            {R.length(R.values(participants))}
          </span>
          <div class="truncate px-1 participants">
            {#if currentUserPartOfKetju}
              <span class="font-semibold text-primary">
                {participants[R.prop('id', whoami)]}
              </span>
            {/if}
            {#each R.compose(R.filter(R.length), R.values, R.dissoc(R.prop('id', whoami)))(participants) as participant}
              <span>
                {participant}
              </span>
            {/each}
          </div>
        </div>
      {/if}
    </div>
  </div>
</a>
