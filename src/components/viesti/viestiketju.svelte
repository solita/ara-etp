<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';

  import { currentUserStore } from '@/stores';

  export let ketju;

  const sentTime = R.compose(R.prop('senttime'), R.last, R.prop('viestit'));

  const participants = R.compose(
    R.reduce(R.mergeLeft, {}),
    R.map(viesti => ({
      [viesti['from-id']]: R.compose(
        Maybe.orSome(viesti.name),
        R.map(R.always('Minä')),
        R.filter(R.propEq('id', viesti['from-id']))
      )($currentUserStore)
    })),
    R.prop('viestit')
  )(ketju);

  console.log($currentUserStore);

  console.log(participants);
</script>

<style>
</style>

<a href={`#/viesti/${ketju.id}`}>
  <div class="flex hover:bg-althover mb-2">
    <div class="flex flex-col w-1/6">
      <span class="block"
        >{R.compose(Formats.formatDateInstant, sentTime)(ketju)}</span>
      <span class="block"
        >{R.compose(Formats.formatHoursMinutes, sentTime)(ketju)}</span>
    </div>
    <div class="flex flex-col w-1/3">
      <span class="block">
        {participants[R.last(ketju.viestit)['from-id']]}
      </span>
      <div class="flex">
        {#if R.length(ketju.viestit) > 1}
          <span class="block w-1/5">{R.length(ketju.viestit)} viestiä</span>
        {:else}
          <span class="block w-1/5">1 viesti</span>
        {/if}
        <span class="block w-4/5">
          {R.compose(
            Maybe.orSome(''),
            R.map(R.compose(R.prop(R.__, participants), R.prop('id')))
          )($currentUserStore)}
          {R.compose(
            R.join(', '),
            R.values,
            Maybe.orSome(participants),
            R.map(R.compose(R.dissoc(R.__, participants), R.prop('id')))
          )($currentUserStore)}
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
