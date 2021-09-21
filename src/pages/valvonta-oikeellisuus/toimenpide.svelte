<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Links from './links';
  import Link from '@Component/Link/Link.svelte';
  import ShowMore from '@Component/show-more/show-more';

  import Deadline from '@Pages/valvonta/deadline';

  import * as valvontaApi from './valvonta-api';
  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';

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
</script>

<!-- purgecss: text-error hover:border-error -->

<div class="flex flex-col space-y-1">
  <div class="flex overflow-hidden">
    <div class="mr-2 whitespace-no-wrap">
      {Formats.formatTimeInstantMinutes(
        Maybe.orSome(toimenpide['create-time'], toimenpide['publish-time'])
      )}
    </div>
    <div class="flex mr-2">
      {#each Maybe.toArray(icon) as icon}
        <span class="font-icon mr-1">{icon}</span>
      {/each}

      <span class="mr-1">
        {#if Toimenpiteet.isDialogType(toimenpide['type-id']) || (Kayttajat.isLaatija(whoami) && Toimenpiteet.isAuditReport(toimenpide))}
          {typeLabel(toimenpide)}
        {:else}
          <Link
            text={typeLabel(toimenpide)}
            href={Links.toimenpide(toimenpide, energiatodistus)}
            {icon} />
        {/if}
      </span>
      {#if Toimenpiteet.isDraft(toimenpide)}
        <span class="font-icon mr-1" title="{i18n(i18nRoot + '.draft')}">mode_edit</span>
      {/if}
      <span>({toimenpide.author.etunimi + toimenpide.author.sukunimi})</span>
    </div>
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

  <Deadline
    {whoami}
    {toimenpide}
    {i18nRoot}
    putToimenpide={valvontaApi.putToimenpide(energiatodistus.id)}
    cancel={() => (toimenpideToUpdate = Maybe.None())}
    {reload} />
</div>
