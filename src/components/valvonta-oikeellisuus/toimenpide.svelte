<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Links from './links';
  import Link from '../Link/Link.svelte';
  import TextButton from '@Component/Button/TextButton';

  import * as valvontaApi from './valvonta-api';

  export let energiatodistus;
  export let toimenpide;
  export let toimenpidetyypit;

  $: typeLabel = R.compose(
    Locales.labelForId($locale, toimenpidetyypit),
    R.prop('type-id')
  );

  let node;
  let truncate = true;

  $: truncated = !!node && node.offsetWidth < node.scrollWidth;
</script>

<div class="flex flex-col mb-3">
  <div class="flex class:items-center={truncated} overflow-hidden">
    <div class="mr-4 whitespace-no-wrap">
      {Formats.formatTimeInstantMinutes(
        Maybe.orSome(toimenpide['create-time'], toimenpide['publish-time'])
      )}
    </div>
    {#if Toimenpiteet.isDialogType(toimenpide['type-id'])}
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
      <div class="flex items-center">
        <span class="font-icon pb-1">alarm</span>
        {Formats.formatDateInstant(deadline)}
      </div>
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
        <TextButton
          on:click={_ => (truncate = !truncate)}
          type={'button'}
          icon={truncate ? 'expand_more' : 'expand_less'}
          text={truncate ? 'Näytä lisää' : 'Näytä vähemmän'} />
      </div>
    {/each}
  {/if}
</div>
