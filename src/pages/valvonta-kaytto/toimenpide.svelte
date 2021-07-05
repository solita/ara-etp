<script>
  import { onMount } from 'svelte';
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import Link from '@Component/Link/Link.svelte';
  import TextButton from '@Component/Button/TextButton';

  import * as valvontaApi from './valvonta-api';

  export let toimenpide;
  export let toimenpidetyypit;

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
</script>

<svelte:window
  on:resize={_ =>
    (truncated = !!node && node.offsetWidth < node.scrollWidth)} />

<div class="flex flex-col mb-3">
  <div class="flex class:items-center={truncated} overflow-hidden">
    <div class="mr-4 whitespace-no-wrap">
      {Formats.formatTimeInstantMinutes(
        Maybe.orSome(toimenpide['create-time'], toimenpide['publish-time'])
      )}
    </div>

    <div class="mr-2">
      {typeLabel(toimenpide)}
    </div>

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
</div>
