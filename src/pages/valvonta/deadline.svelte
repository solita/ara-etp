<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as dfns from 'date-fns';

  import { _ } from '@Language/i18n';

  import DeadlineDialog from '@Pages/valvonta/deadline-dialog';

  const i18n = $_;

  export let whoami;
  export let putToimenpide;
  export let toimenpide;
  export let i18nRoot;
  export let reload;

  let toimenpideToUpdate = Maybe.None();

  const isPastDeadline = R.both(R.complement(dfns.isToday), dfns.isPast);
</script>

<!-- purgecss: text-error hover:border-error -->

{#each Maybe.toArray(toimenpideToUpdate) as toimenpide}
  <DeadlineDialog
    {toimenpide}
    {i18nRoot}
    {putToimenpide}
    cancel={() => (toimenpideToUpdate = Maybe.None())}
    {reload} />
{/each}

{#each EM.toArray(toimenpide['deadline-date']) as deadline}
  <div>
    {#if Kayttajat.isPaakayttaja(whoami)}
      <button
        on:click={_ => (toimenpideToUpdate = Maybe.Some(toimenpide))}
        class="inline-flex space-x-1 font-bold items-center text-primary border-b-1 border-transparent hover:border-primary cursor-pointer"
        class:text-error={isPastDeadline(deadline)}
        class:hover:border-error={isPastDeadline(deadline)}>
        <span class="font-icon">schedule</span>
        <span>Määräaika {Formats.formatDateInstant(deadline)}</span>
        <span class="font-icon">edit</span>
      </button>
    {:else}
      <div
        class="inline-flex space-x-1 items-center border-b-1 border-transparent"
        class:text-error={isPastDeadline(deadline)}>
        <span class="font-icon">schedule</span>
        <span>Määräaika {Formats.formatDateInstant(deadline)}</span>
      </div>
    {/if}
  </div>
{/each}
