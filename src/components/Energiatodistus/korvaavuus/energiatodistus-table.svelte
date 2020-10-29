<script>
  import * as R from 'ramda';

  import { push } from '@Component/Router/router';
  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as str from '@Utility/strings';

  export let energiatodistus;
  
</script>

<style type="text/postcss">
</style>

<table class="etp-table">
  <thead class="etp-table--thead">
  <tr class="etp-table--tr etp-table--tr__light">
    <th class="etp-table--th">
      {$_('energiatodistus.korvaavuus.table.tunnus')}
    </th>
    <th class="etp-table--th">
      {$_('energiatodistus.korvaavuus.table.etl')}
    </th>
    <th class="etp-table--th">
      {$_('energiatodistus.korvaavuus.table.ktl')}
    </th>
    <th class="etp-table--th">
      {$_('energiatodistus.korvaavuus.table.rakennustunnus')}
    </th>
    <th class="etp-table--th">
      {$_('energiatodistus.korvaavuus.table.nettoala')}
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
    <tr class="etp-table-tr etp-table--tr__link"
        on:click={() => push('#/energiatodistus/' + energiatodistus.versio + '/' + energiatodistus.id)}>
      <td class="etp-table--td">
        {energiatodistus.id}
      </td>
      <td class="etp-table--td">
        {Maybe.orSome('-', energiatodistus.tulokset['e-luokka'])}<sub>{energiatodistus.versio}</sub>
      </td>
      <td class="etp-table--td">
        {Maybe.orSome('', energiatodistus.perustiedot.kayttotarkoitus)}
      </td>
      <td class="etp-table--td">
        {Maybe.orSome('', energiatodistus.perustiedot.rakennustunnus)}
      </td>
      <td class="etp-table--td">
        {EM.orSome('', energiatodistus.lahtotiedot['lammitetty-nettoala'])}
      </td>
      <td class="etp-table--td">
        {Maybe.orSome('', energiatodistus.perustiedot.nimi)}
      </td>
      <td class="etp-table--td">
        {Maybe.orSome('', energiatodistus.perustiedot['katuosoite-fi'])}
        {Maybe.fold('', R.compose(str.lpad(5, '0'), n => n.toString()),
          energiatodistus.perustiedot.postinumero)}
      </td>
      <td class="etp-table--td">
        {Maybe.orSome('', energiatodistus['laatija-fullname'])}
      </td>
    </tr>
  </tbody>
</table>