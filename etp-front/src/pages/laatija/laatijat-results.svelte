<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Future from '@Utility/future-utils';
  import { push } from '@Component/Router/router';

  import { locale, _ } from '@Language/i18n';

  import H2 from '@Component/H/H2';
  import Pagination from '@Component/Pagination/Pagination.svelte';

  const i18n = $_;
  const i18nRoot = 'laatijat';

  export let page = 1;
  export let toPage;
  export let itemsPerPage = 10;
  export let laatijatFuture;
  export let yritykset;
  export let patevyydet;
  export let toimintaalueet;
  export let whoami;

  let laatijat = [];

  let cancel = () => {};
  const search = future => {
    cancel();
    cancel = Future.value(result => {
      laatijat = result;
      toPage(1);
    }, future);
  };

  $: search(laatijatFuture);
</script>

<div class="mt-10">
  <H2
    dataCy="laatijat-results-header"
    text={i18n(i18nRoot + '.results', {
      values: { count: R.length(laatijat) }
    })} />
</div>

{#if !R.isEmpty(laatijat)}
  <div class="overflow-x-auto">
    <table class="etp-table">
      <thead class="etp-table--thead">
        <th class="etp-table--th">{i18n('laatija.laatija')}</th>
        <th class="etp-table--th">{i18n('kayttaja.puhelin')}</th>
        <th class="etp-table--th">{i18n('laatija.patevyystaso')}</th>
        <th class="etp-table--th">{i18n(i18nRoot + '.voimassaolo')}</th>
        <th class="etp-table--th">{i18n('laatija.paatoimintaalue')}</th>
        <th class="etp-table--th">{i18n('laatija.postinumero')}</th>
        <th class="etp-table--th">{i18n('laatija.postitoimipaikka')}</th>
        <th class="etp-table--th">{i18n('yritys.yritykset')}</th>
        {#if Kayttajat.isPaakayttajaOrLaskuttaja(whoami)}
          <th class="etp-table--th">
            {$_(i18nRoot + '.energiatodistukset')}
          </th>
        {/if}
      </thead>
      <tbody class="etp-table--tbody">
        {#each R.slice(R.dec(page) * itemsPerPage, page * itemsPerPage, laatijat) as laatija}
          <tr
            data-cy="laatija-row"
            class="etp-table--tr etp-table--tr__link"
            on:click={() => push('#/kayttaja/' + laatija.id)}>
            <td class="etp-table--td">
              {laatija.etunimi}
              {laatija.sukunimi}
            </td>
            <td class="etp-table--td">
              {laatija.puhelin}
            </td>
            <td class="etp-table--td">
              {Locales.labelForId($locale, patevyydet)(laatija.patevyystaso)}
            </td>
            <td class="etp-table--td">
              {Formats.formatDateInstant(laatija.toteamispaivamaara)} -
              {Formats.inclusiveEndDate(laatija['voimassaolo-paattymisaika'])}
            </td>
            <td class="etp-table--td">
              {Maybe.fold(
                '',
                Locales.labelForId($locale, toimintaalueet),
                laatija.toimintaalue
              )}
            </td>
            <td class="etp-table--td">
              {laatija.postinumero}
            </td>
            <td class="etp-table--td">
              {laatija.postitoimipaikka}
            </td>
            <td class="etp-table--td" on:click|stopPropagation>
              {#each R.filter(R.propSatisfies(R.includes(R.__, laatija.yritys), 'id'), yritykset) as { id, nimi }}
                <a class="text-primary hover:underline" href={`#/yritys/${id}`}>
                  {nimi}
                </a>
              {/each}
            </td>
            {#if Kayttajat.isPaakayttajaOrLaskuttaja(whoami)}
              <td class="etp-table--td" on:click|stopPropagation>
                <a
                  class="font-icon text-2xl text-primary hover:underline"
                  href={`#/energiatodistus/all?where=[[["=","energiatodistus.laatija-id",${laatija.id}]]]`}>
                  view_list
                </a>
              </td>
            {/if}
          </tr>
        {/each}
      </tbody>
    </table>
  </div>

  <div class="mt-2">
    <Pagination
      pageNum={page}
      {itemsPerPage}
      nextPageCallback={toPage}
      itemsCount={R.length(laatijat)} />
  </div>
{/if}
