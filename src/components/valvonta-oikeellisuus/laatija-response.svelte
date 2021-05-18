<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';
  import * as Router from '@Component/Router/router';

  import { _, locale } from '@Language/i18n';

  import * as ValvontaApi from '@Component/valvonta-oikeellisuus/valvonta-api';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Links from './links';

  import H2 from '@Component/H/H2.svelte';
  import Link from '@Component/Link/Link.svelte';
  import TextButton from '@Component/Button/TextButton.svelte';

  export let toimenpiteet;
  export let energiatodistus;
  export let fork;

  const last = R.compose(Maybe.fromNull, R.last);

  const newResponse = (responseType, energiatodistus) =>
    fork(
      'new-response',
      response => Router.push(Links.toimenpide(response, energiatodistus)))
      (ValvontaApi.postToimenpide(
        energiatodistus.id,
        Toimenpiteet.emptyToimenpide(responseType)));
</script>

<H2 text="Vastaus" />

<div class="mb-5">
  {#each [...last(toimenpiteet)] as lastToimenpide}
    {#each [...Toimenpiteet.responseTypeFor(lastToimenpide)] as responseType}
      <div>
        <p class="mb-2">
          Laatijalta pyydetään vastausta liitteineen tietopyyntöön
          {EM.fold(
            '',
            Formats.formatDateInstant,
            lastToimenpide['deadline-date']
          )}
          mennessä.
        </p>

        <div class="flex">
          <TextButton
            on:click={newResponse(responseType, energiatodistus)}
            icon="create"
            text="Aloita vastauksen tekeminen tietopyyntöön." />
        </div>
      </div>
    {/each}

    {#if Toimenpiteet.isResponse(lastToimenpide) && Toimenpiteet.isDraft(lastToimenpide)}
      <p class="mb-2">
        Laatijalta pyydetään vastausta liitteineen tietopyyntöön
        {EM.fold(
          '',
          Formats.formatDateInstant,
          toimenpiteet[R.length(toimenpiteet) - 2]['deadline-date']
        )}
        mennessä.
      </p>
      <p class="mb-2">
        Kun vastaus ja liitteet on tallennettu järjestelmään, muista lopuksi
        vielä lähettää vastaus.
      </p>
      <div class="flex">
        <Link
          href={Links.toimenpide(lastToimenpide, energiatodistus)}
          icon={Maybe.Some('edit')}
          text="Jatka vastauksen tekemistä tietopyyntöön." />
      </div>
    {:else if Toimenpiteet.isResponse(lastToimenpide) && !Toimenpiteet.isDraft(lastToimenpide)}
      <p class="mb-2">
        Laatija on lähettänyt
        <span class="inline-block">
          <Link
            href={Links.toimenpide(lastToimenpide, energiatodistus)}
            text="vastauksen" />
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
