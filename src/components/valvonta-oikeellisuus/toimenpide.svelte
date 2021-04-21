<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import Link from '../Link/Link.svelte';

  export let energiatodistus;
  export let toimenpide;
  export let toimenpidetyypit;

  $: typeLabel = R.compose(
    Locales.labelForId($locale, toimenpidetyypit),
    R.prop('type-id')
  );
</script>

<div class="flex">
  <div class="mr-4">
  {Formats.formatTimeInstantMinutes(Maybe.orSome(
    toimenpide['create-time'],
    toimenpide['publish-time']))}
  </div>
  <div class="flex">
    {#if Toimenpiteet.isDialogType(toimenpide['type-id'])}
      {typeLabel(toimenpide)}
    {:else}
      <Link text={typeLabel(toimenpide)}
            href={`#/valvonta/oikeellisuus/${energiatodistus.version}/${energiatodistus.id}/${toimenpide.id}`}/>
    {/if}
    {#if Toimenpiteet.isDraft(toimenpide)}
      <div class="ml-2">(luonnos)</div>
    {/if}
  </div>
</div>
