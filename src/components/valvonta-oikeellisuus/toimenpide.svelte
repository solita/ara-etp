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

  export let energiatodistus;
  export let toimenpide;
  export let toimenpidetyypit;

  $: typeLabel = R.compose(
    Locales.labelForId($locale, toimenpidetyypit),
    R.prop('type-id')
  );
</script>

<div class="flex items-center overflow-hidden">
  <div class="mr-4 whitespace-no-wrap">
    {Formats.formatTimeInstantMinutes(Maybe.orSome(
      toimenpide['create-time'],
      toimenpide['publish-time']))}
  </div>
  {#if Toimenpiteet.isDialogType(toimenpide['type-id'])}
    <div class="mr-2">
    {typeLabel(toimenpide)}
    </div>
  {:else}
    <div class="mr-2">
      <Link text={typeLabel(toimenpide)}
            href={Links.toimenpide(toimenpide, energiatodistus)}/>
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
  {#each Maybe.toArray(toimenpide.document) as document}
    <div class="truncate">
      - {document}
    </div>
  {/each}
</div>
