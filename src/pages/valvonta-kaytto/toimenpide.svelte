<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import * as ValvontaApi from './valvonta-api';

  import Osapuoli from './toimenpide-osapuoli.svelte';
  import Deadline from '@Pages/valvonta/deadline';
  import ShowMore from '@Component/show-more/show-more';
  import * as Osapuolet from '@Pages/valvonta-kaytto/osapuolet';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.valvonta.toimenpide';

  export let whoami;
  export let valvonta;
  export let toimenpide;
  export let toimenpidetyypit;
  export let roolit;
  export let toimitustavat;
  export let reload;

  $: typeLabel = R.compose(
    Locales.labelForId($locale, toimenpidetyypit),
    R.prop('type-id')
  );
</script>

<div class="flex flex-col mb-3">
  <div class="flex overflow-hidden items-center">
    <div class="mr-4 whitespace-no-wrap">
      {Formats.formatTimeInstantMinutes(
        Maybe.orSome(toimenpide['create-time'], toimenpide['publish-time'])
      )}
    </div>

    <div class="mr-2">
      {typeLabel(toimenpide)}
    </div>

    {#each EM.toArray(toimenpide['deadline-date']) as deadline}
      <div class="flex items-center mr-1">
        <span class="font-icon pb-1">alarm</span>
        {Formats.formatDateInstant(deadline)}
      </div>
    {/each}
    <span> ({toimenpide.author.etunimi} {toimenpide.author.sukunimi}) </span>
  </div>

  {#if !Toimenpiteet.isDraft(toimenpide) && Toimenpiteet.hasTemplate(toimenpide)}
    {#each toimenpide.henkilot as henkilo}
      {#if Toimenpiteet.hasOptionalDocument(toimenpide) && !Toimenpiteet.documentExistsForOsapuoli(toimenpide, henkilo.id, Osapuolet.getOsapuoliType(henkilo))}
        <div>
          {henkilo.etunimi}
          {henkilo.sukunimi},
          {i18n(i18nRoot + '.no-document')}
        </div>
      {:else}
        <div>
          {henkilo.etunimi}
          {henkilo.sukunimi}
          <Osapuoli
            osapuoli={henkilo}
            type="henkilo"
            {valvonta}
            {toimenpide}
            {roolit}
            {toimitustavat} />
        </div>
      {/if}
    {/each}
    {#each toimenpide.yritykset as yritys}
      {#if Toimenpiteet.hasOptionalDocument(toimenpide) && !Toimenpiteet.documentExistsForOsapuoli(toimenpide, yritys.id, Osapuolet.getOsapuoliType(yritys))}
        <div>
          {yritys.nimi}
          {Maybe.orSome('', yritys.ytunnus)},
          {i18n(i18nRoot + '.no-document')}
        </div>
      {:else}
        <div>
          {yritys.nimi}
          {Maybe.orSome('', yritys.ytunnus)}
          <Osapuoli
            osapuoli={yritys}
            type="yritys"
            {valvonta}
            {toimenpide}
            {roolit}
            {toimitustavat} />
        </div>
      {/if}
    {/each}
  {/if}

  {#each Maybe.toArray(toimenpide.description) as description}
    <div class="mt-1">
      <ShowMore>
        <p>{description}</p>
      </ShowMore>
    </div>
  {/each}

  <Deadline
    {whoami}
    {toimenpide}
    i18nRoot={'valvonta.kaytto.toimenpide'}
    putToimenpide={ValvontaApi.putToimenpide(valvonta.id)}
    {reload} />
</div>
