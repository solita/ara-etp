<script>
  import { onMount } from 'svelte';
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Kayttajat from '@Utility/kayttajat';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Links from './links';
  import Link from '../Link/Link.svelte';
  import TextButton from '@Component/Button/TextButton';

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

  let node;
  let truncate = true;

  let truncated;

  onMount(() => {
    truncated = !!node && node.offsetWidth < node.scrollWidth;
  });

  let toimenpideToUpdate = Maybe.None();
</script>

<svelte:window
  on:resize={_ =>
    (truncated = !!node && node.offsetWidth < node.scrollWidth)} />

{#each Maybe.toArray(toimenpideToUpdate) as toimenpide}
  <ChangeDeadlineDialog
    {energiatodistus}
    {toimenpide}
    {i18n}
    cancel={() => (toimenpideToUpdate = Maybe.None())}
    {reload} />
{/each}

<div class="flex flex-col mb-3">
  <div class="flex class:items-center={truncated} overflow-hidden">
    <div class="mr-4 whitespace-no-wrap">
      {Formats.formatTimeInstantMinutes(
        Maybe.orSome(toimenpide['create-time'], toimenpide['publish-time'])
      )}
    </div>
    {#if Toimenpiteet.isDialogType(toimenpide['type-id']) || (Kayttajat.isLaatija(whoami) && Toimenpiteet.isAuditReport(toimenpide))}
      <div class="mr-2">
        {typeLabel(toimenpide)}
      </div>
    {:else}
      <div class="mr-2">
        <Link
          text={typeLabel(toimenpide)}
          href={Links.toimenpide(toimenpide, energiatodistus)} />
      </div>
    {/if}
    {#if Toimenpiteet.isDraft(toimenpide)}
      <div class="ml-2">(luonnos)</div>
    {/if}
    {#each EM.toArray(toimenpide['deadline-date']) as deadline}
      {#if Kayttajat.isPaakayttaja(whoami)}
        <div
          on:click={_ => (toimenpideToUpdate = Maybe.Some(toimenpide))}
          class="flex items-center text-primary border-b-1 border-transparent hover:border-primary cursor-pointer">
          <span class="font-icon">alarm</span>
          {Formats.formatDateInstant(deadline)}
        </div>
      {:else}
        <div class="flex items-center border-b-1 border-transparent">
          <span class="font-icon">alarm</span>
          {Formats.formatDateInstant(deadline)}
        </div>
      {/if}
    {/each}

    {#if !Toimenpiteet.isDraft(toimenpide) && Toimenpiteet.hasTemplate(toimenpide)}
      <div class="ml-2">
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
  </div>

  {#if !Toimenpiteet.isResponse(toimenpide)}
    {#each Maybe.toArray(toimenpide.description) as description}
      <div class="mt-1 min-w-0 flex flex-wrap">
        <p bind:this={node} class:truncate>
          {description}
        </p>
        {#if truncated}
          <TextButton
            on:click={_ => (truncate = !truncate)}
            type={'button'}
            icon={truncate ? 'expand_more' : 'expand_less'}
            text={truncate ? 'Näytä lisää' : 'Näytä vähemmän'} />
        {/if}
      </div>
    {/each}
  {/if}
</div>
