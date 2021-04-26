<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';

  import * as Toimenpiteet from './toimenpiteet';

  import H2 from '@Component/H/H2.svelte';
  import Link from '@Component/Link/Link.svelte';

  export let toimenpiteet;
  export let energiatodistus;

  const last = R.compose(Maybe.fromNull, R.last);
</script>

<H2 text="Vastaus" />

<div class="mb-5">
{#each [... last(toimenpiteet)] as lastToimenpide}
  {#each [... Toimenpiteet.responseTypeFor(lastToimenpide)] as responseType}
    <div>
      <p class="mb-2">
        Laatijalta pyydetään vastausta liitteineen tietopyyntöön
        {EM.fold('', Formats.formatDateInstant, lastToimenpide['deadline-date'])}
        mennessä.
      </p>

      <div class="flex">
        <Link href="#/valvonta/oikeellisuus/{energiatodistus.versio}/{energiatodistus.id}/new/{responseType}"
              icon={Maybe.Some('create')}
              text="Aloita vastauksen tekeminen tietopyyntöön."/>
      </div>
    </div>
  {/each}

  {#if Toimenpiteet.isResponse(lastToimenpide) && Toimenpiteet.isDraft(lastToimenpide)}
    <p class="mb-2">
      Laatijalta pyydetään vastausta liitteineen tietopyyntöön
      {EM.fold('', Formats.formatDateInstant, toimenpiteet[R.length(toimenpiteet) - 2]['deadline-date'])}
      mennessä.
    </p>
    <p class="mb-2">
      Kun vastaus ja liitteet on tallennettu järjestelmään, muista lopuksi vielä lähettää vastaus.
    </p>
    <div class="flex">
      <Link href="#/valvonta/oikeellisuus/{energiatodistus.versio}/{energiatodistus.id}/{lastToimenpide.id}"
            icon={Maybe.Some('edit')}
            text="Jatka vastauksen tekemistä tietopyyntöön."/>
    </div>
  {:else if Toimenpiteet.isResponse(lastToimenpide) && !Toimenpiteet.isDraft(lastToimenpide)}
    <p class="mb-2">
      Laatija on lähettänyt
      <span class="inline-block">
        <Link href="#/valvonta/oikeellisuus/{energiatodistus.versio}/{energiatodistus.id}/{lastToimenpide.id}"
              text="vastauksen"/>
      </span>
      tietopyyntöön.
    </p>
  {:else if Maybe.isNone(Toimenpiteet.responseTypeFor(lastToimenpide))}
    Laatijalla ei ole keskeneräisiä toimenpiteitä.
  {/if}
{/each}

{#if R.isEmpty(toimenpiteet)}
  Laatijalla ei ole keskeneräisiä toimenpiteitä.
{/if}
</div>