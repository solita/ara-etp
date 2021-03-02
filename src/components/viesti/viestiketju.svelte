<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';

  import * as Viestit from './viesti-util';

  import { _ } from '@Language/i18n';

  export let ketju;
  export let whoami;

  const sentTime = R.compose(R.prop('senttime'), R.last, R.prop('viestit'));

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
    @apply border-t;
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
</style>

<!-- purgecss: font-bold text-primary -->

<a
  href={`#/viesti/${ketju.id}`}
  class="flex flex-col border-b border-background hover:bg-background hover:rounded-lg px-2 py-5">
  <div class="flex items-start font-bold">
    <span class="w-1/12 py-2 border border-transparent">
      {R.compose(Formats.formatDateInstant, sentTime)(ketju)}
    </span>
    <div class="w-5/12 py-2 flex">
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
      class="flex w-1/2 justify-between"
      class:expanding-subject={R.gt(R.length(R.prop('subject', ketju)), 55)}>
      <span class="p-2 subject"> {ketju.subject} </span>

      {#if R.gt(R.length(ketju.viestit), 1)}
        <div
          class="flex items-center flex-shrink ml-auto justify-self-end py-2 border border-transparent">
          <span class="font-icon outline mr-1">chat_bubble_outline</span>
          <span class="font-semibold">{R.length(ketju.viestit)}</span>
        </div>
      {/if}
    </div>
  </div>

  <div class="flex items-start text-sm">
    <div class="flex flex-col w-1/12">
      <span class="block">
        {R.compose(Formats.formatHoursMinutes, sentTime)(ketju)}
      </span>
    </div>

    <div class="flex w-5/12 items-center space-x-2">
      {#if R.gt(R.length(R.values(participants)), 1)}
        <div class="flex items-center w-full">
          <span class="font-icon font-semibold">people</span>
          <span class="font-semibold mx-1">
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

    <div class="flex w-1/2">
      <span class="truncate px-2">
        {R.last(ketju.viestit).body}
      </span>
    </div>
  </div>
</a>
