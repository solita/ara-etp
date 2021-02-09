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
  .participant:not(:first-child)::before {
    content: ', ';
  }
</style>

<!-- purgecss: font-bold text-primary -->

<a href={`#/viesti/${ketju.id}`}>
  <div class="flex hover:bg-althover border-b-1 py-2 border-dark">
    <div class="flex flex-col w-1/6">
      <span class="block"
        >{R.compose(Formats.formatDateInstant, sentTime)(ketju)}</span>
      <span class="block"
        >{R.compose(Formats.formatHoursMinutes, sentTime)(ketju)}</span>
    </div>
    <div class="flex flex-col w-1/3">
      <span
        class="block font-bold"
        class:text-primary={R.propEq(
          'id',
          R.path(['from', 'id'], R.last(ketju.viestit)),
          whoami
        )}>
        {participants[R.path(['from', 'id'], R.last(ketju.viestit))]}
      </span>
      <div class="flex">
        {#if R.length(ketju.viestit) > 1}
          <span class="block w-4/12">{R.length(ketju.viestit)} viestiä</span>
        {:else}
          <span class="block w-4/12">1 viesti</span>
        {/if}
        <div class="w-8/12 flex">
          <span class="block font-icon">people</span>
          <div class="flex-auto ml-1">
            {#if currentUserPartOfKetju}
              <span class="participant font-bold text-primary">
                {participants[R.prop('id', whoami)]}
              </span>
            {/if}
            {#each R.compose(R.filter(R.length), R.values, R.dissoc(R.prop('id', whoami)))(participants) as participant}
              <span class="participant">
                {participant}
              </span>
            {/each}
          </div>
        </div>
      </div>
    </div>
    <div class="flex w-1/2">
      <span class="font-bold">{ketju.subject}</span>
      <span class="truncate">
        &nbsp;- {R.last(ketju.viestit).body}
      </span>
    </div>
  </div>
</a>
