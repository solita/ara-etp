<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';

  import * as Viestit from './viesti-util';

  import { currentUserStore } from '@/stores';
  import { _ } from '@Language/i18n';

  export let ketju;

  const sentTime = R.compose(R.prop('senttime'), R.last, R.prop('viestit'));

  $: participants = R.compose(
    R.reduce(R.mergeLeft, {}),
    R.tap(console.log),
    R.map(viesti => {
      if (Maybe.exists(Viestit.isSelfSent(viesti), $currentUserStore)) {
        return { [viesti.from.id]: $_('viesti.mina') };
      }
      return { [viesti.from.id]: Viestit.formatSender($_, viesti.from) };
    }),
    R.prop('viestit')
  )(ketju);
</script>

<style>
</style>

<a href={`#/viesti/${ketju.id}`}>
  <div class="flex hover:bg-althover mb-2 border-b-1 pt-2 border-dark">
    <div class="flex flex-col w-1/6">
      <span class="block"
        >{R.compose(Formats.formatDateInstant, sentTime)(ketju)}</span>
      <span class="block"
        >{R.compose(Formats.formatHoursMinutes, sentTime)(ketju)}</span>
    </div>
    <div class="flex flex-col w-1/3">
      <span class="block">
        {participants[R.path(['from', 'id'], R.last(ketju.viestit))]}
      </span>
      <div class="flex">
        {#if R.length(ketju.viestit) > 1}
          <span class="block w-4/12">{R.length(ketju.viestit)} viestiä</span>
        {:else}
          <span class="block w-4/12">1 viesti</span>
        {/if}
        <span class="block w-8/12">
          <span class="font-icon">people</span>
          {R.join(', ', R.filter(R.length, [
            R.compose(
              Maybe.orSome(''),
              R.chain(
                Maybe.nullReturning(
                  R.compose(R.prop(R.__, participants), R.prop('id'))
                )
              )
            )($currentUserStore),
            R.compose(
              R.values,
              Maybe.orSome(participants),
              R.chain(
                Maybe.nullReturning(
                  R.compose(R.dissoc(R.__, participants), R.prop('id'))
                )
              )
            )($currentUserStore)
          ]))}
        </span>
      </div>
    </div>
    <div class="flex flex-col w-1/2">
      <span class="truncate">
        {ketju.subject} - {R.last(ketju.viestit).body}
      </span>
    </div>
  </div>
</a>
