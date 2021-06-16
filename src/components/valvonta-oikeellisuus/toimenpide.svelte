<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as dfns from 'date-fns';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Links from './links';
  import Link from '../Link/Link.svelte';
  import ShowMore from '@Component/show-more/show-more';

  import ChangeDeadlineDialog from './change-deadline-dialog';

  import * as valvontaApi from './valvonta-api';

  export let whoami;
  export let energiatodistus;
  export let toimenpide;
  export let toimenpidetyypit;
  export let i18n;
  export let reload;

  $: typeLabel = R.compose(
    Locales.labelForId($locale, toimenpidetyypit),
    R.prop('type-id')
  );

  let toimenpideToUpdate = Maybe.None();
  let icon = Maybe.None();

  $: if (Toimenpiteet.isVerified(toimenpide)) {
    icon = Maybe.Some('visibility');
  } else if (Toimenpiteet.isAnomaly(toimenpide)) {
    icon = Maybe.Some('bug_report');
  }

  const isPastDeadline = R.both(R.complement(dfns.isToday), dfns.isPast);
</script>

<!-- purgecss: text-error hover:border-error -->

{#each Maybe.toArray(toimenpideToUpdate) as toimenpide}
  <ChangeDeadlineDialog
    {energiatodistus}
    {toimenpide}
    {i18n}
    cancel={() => (toimenpideToUpdate = Maybe.None())}
    {reload} />
{/each}

<div class="flex flex-col space-y-1">
  <div class="flex overflow-hidden">
    <div class="mr-2 whitespace-no-wrap">
      {Formats.formatTimeInstantMinutes(
        Maybe.orSome(toimenpide['create-time'], toimenpide['publish-time'])
      )}
    </div>
    {#if Toimenpiteet.isDialogType(toimenpide['type-id']) || (Kayttajat.isLaatija(whoami) && Toimenpiteet.isAuditReport(toimenpide))}
      <div class="mr-2">
        {#each Maybe.toArray(icon) as icon}
          <span class="font-icon">{icon}</span>
        {/each}
        {typeLabel(toimenpide)}
        {`(${toimenpide.author.etunimi} ${toimenpide.author.sukunimi})`}
      </div>
    {:else}
      <div class="mr-2">
        <Link
          text={typeLabel(toimenpide)}
          href={Links.toimenpide(toimenpide, energiatodistus)}
          {icon} />
      </div>
    {/if}
    {#if Toimenpiteet.isDraft(toimenpide)}
      <div class="ml-2">(luonnos)</div>
    {/if}
  </div>

  {#if !Toimenpiteet.isResponse(toimenpide)}
    {#each Maybe.toArray(toimenpide.description) as description}
      <ShowMore>
        <p>
          {description}
        </p>
      </ShowMore>
    {/each}
  {/if}

  {#if !Toimenpiteet.isDraft(toimenpide) && Toimenpiteet.hasTemplate(toimenpide)}
    <div>
      <Link
        text={toimenpide.filename}
        target={'_blank'}
        href={valvontaApi.url.document(
          toimenpide['energiatodistus-id'],
          toimenpide.id,
          toimenpide['filename']
        )} />
    </div>
  {/if}

  {#each EM.toArray(toimenpide['deadline-date']) as deadline}
    <div>
      {#if Kayttajat.isPaakayttaja(whoami)}
        <button
          on:click={_ => (toimenpideToUpdate = Maybe.Some(toimenpide))}
          class="inline-block font-bold items-center text-primary border-b-1 border-transparent hover:border-primary cursor-pointer"
          class:text-error={isPastDeadline(deadline)}
          class:hover:border-error={isPastDeadline(deadline)}>
          <span class="font-icon">schedule</span>
          {Formats.formatDateInstant(deadline)}
          <span class="font-icon">edit</span>
        </button>
      {:else}
        <span
          class="border-b-1 border-transparent"
          class:text-error={isPastDeadline(deadline)}>
          <span class="font-icon">schedule</span>
          {Formats.formatDateInstant(deadline)}
        </span>
      {/if}
    </div>
  {/each}
</div>
