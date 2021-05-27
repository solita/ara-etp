<script>
  import * as R from 'ramda';

  import { push } from '@Component/Router/router';
  import { _, locale } from '@Language/i18n';

  import * as Postinumerot from '../postinumero';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as str from '@Utility/strings';

  export let energiatodistus;
  export let postinumerot;
</script>

<style type="text/postcss">
  address {
    @apply not-italic;
  }
</style>

<div class="overflow-x-auto">
  <table class="etp-table">
    <thead class="etp-table--thead">
      <tr class="etp-table--tr etp-table--tr__light">
        <th class="etp-table--th">
          {$_('energiatodistus.korvaavuus.table.tunnus')}
        </th>
        <th class="etp-table--th">
          {$_('energiatodistus.korvaavuus.table.ktl')}
        </th>
        <th class="etp-table--th">
          {$_('energiatodistus.korvaavuus.table.rakennustunnus')}
        </th>
        <th class="etp-table--th">
          {$_('energiatodistus.korvaavuus.table.nimi')}
        </th>
        <th class="etp-table--th">
          {$_('energiatodistus.korvaavuus.table.osoite')}
        </th>
        <th class="etp-table--th">
          {$_('energiatodistus.korvaavuus.table.laatija')}
        </th>
      </tr>
    </thead>
    <tbody class="etp-table--tbody">
      <tr
        class="etp-table-tr etp-table--tr__link"
        on:click={() =>
          push(
            '#/energiatodistus/' +
              energiatodistus.versio +
              '/' +
              energiatodistus.id
          )}>
        <td class="etp-table--td">
          {energiatodistus.id}
        </td>
        <td class="etp-table--td">
          {Maybe.orSome('', energiatodistus.perustiedot.kayttotarkoitus)}
        </td>
        <td class="etp-table--td">
          {Maybe.orSome('', energiatodistus.perustiedot.rakennustunnus)}
        </td>
        <td class="etp-table--td">
          {Maybe.orSome('', energiatodistus.perustiedot.nimi)}
        </td>
        <td class="etp-table--td">
          <address>
            {Maybe.orSome('', energiatodistus.perustiedot['katuosoite-fi'])}
            <span class="whitespace-no-wrap">
              {Maybe.fold(
                '',
                Postinumerot.formatPostinumero(postinumerot, $locale),
                energiatodistus.perustiedot.postinumero
              )}
            </span>
          </address>
        </td>
        <td class="etp-table--td">
          {Maybe.orSome('', energiatodistus['laatija-fullname'])}
        </td>
      </tr>
    </tbody>
  </table>
</div>
