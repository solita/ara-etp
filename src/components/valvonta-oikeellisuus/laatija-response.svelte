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
  const request = R.nth(-2);

  const i18nRoot = 'valvonta.oikeellisuus.laatija-response';
  const i18n = $_;
  const toimenpideText = R.compose(i18n, Toimenpiteet.i18nKey);

  const deadlineDate = R.compose(
    EM.fold('', Formats.formatDateInstant),
    R.prop('deadline-date')
  );

  const newResponse = (responseType, energiatodistus) =>
    fork('new-response', response =>
      Router.push(Links.toimenpide(response, energiatodistus))
    )(
      ValvontaApi.postToimenpide(
        energiatodistus.id,
        Toimenpiteet.emptyToimenpide(responseType)
      )
    );
</script>

{#each [...last(toimenpiteet)] as lastToimenpide}
  {#if !Toimenpiteet.isDraft(lastToimenpide)}
    {#each [...Toimenpiteet.responseTypeFor(lastToimenpide)] as responseType}
      <H2 text={toimenpideText(lastToimenpide, 'title')} />
      <div class="mb-5">
        {#if Toimenpiteet.hasTemplate(lastToimenpide)}
          <div class="mb-4">
            <Link
              text={lastToimenpide.filename}
              target={'_blank'}
              href={ValvontaApi.url.document(
                lastToimenpide['energiatodistus-id'],
                lastToimenpide.id,
                lastToimenpide['filename']
              )} />
          </div>
        {/if}
        <p class="mb-2">
          {R.replace(
            '{deadline-date}',
            deadlineDate(lastToimenpide),
            toimenpideText(lastToimenpide, 'response-description')
          )}
        </p>

        <div class="flex">
          <TextButton
            on:click={newResponse(responseType, energiatodistus)}
            icon="create"
            text={i18n(i18nRoot + '.start-button')} />
        </div>
      </div>
    {/each}
  {/if}

  {#if Toimenpiteet.isResponse(lastToimenpide) && Toimenpiteet.isDraft(lastToimenpide)}
    <H2 text={toimenpideText(request(toimenpiteet), 'title')} />
    <div class="mb-5">
      <p class="mb-2">
        {R.replace(
          '{deadline-date}',
          deadlineDate(request(toimenpiteet)),
          toimenpideText(request(toimenpiteet), 'response-description')
        )}
      </p>
      <p class="mb-2">
        {i18n(i18nRoot + '.note')}
      </p>
      <div class="flex">
        <Link
          href={Links.toimenpide(lastToimenpide, energiatodistus)}
          icon={Maybe.Some('edit')}
          text={i18n(i18nRoot + '.continue-link')} />
      </div>
    </div>
  {:else if Toimenpiteet.isResponse(lastToimenpide) && !Toimenpiteet.isDraft(lastToimenpide)}
    <H2 text={toimenpideText(request(toimenpiteet), 'title')} />
    <p class="mb-5">
      {R.replace(
        '{publish-date}',
        Maybe.fold(
          '',
          Formats.formatTimeInstantMinutes,
          lastToimenpide['publish-time']
        ),
        i18n(i18nRoot + '.response-sent-description')
      )}
      <span class="inline-block">
        <Link
          href={Links.toimenpide(lastToimenpide, energiatodistus)}
          text={i18n(i18nRoot + '.response-sent-link')} />
      </span>
    </p>
  {/if}
{/each}
