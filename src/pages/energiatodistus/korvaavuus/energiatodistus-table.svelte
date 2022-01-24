<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import { push } from '@Component/Router/router';
  import { _ } from '@Language/i18n';

  import Address from '@Pages/energiatodistus/address.svelte';
  import RakennuksenNimi from '@Pages/energiatodistus/RakennuksenNimi';

  export let whoami;
  export let energiatodistus;
  export let postinumerot;

  $: hasPermission =
    !Kayttajat.isLaatija(whoami) ||
    Maybe.fold(false, R.equals(whoami.id), energiatodistus['laatija-id']);
</script>

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
    <tbody>
      <tr
        class:etp-table--tr__link={hasPermission}
        class="etp-table-tr"
        on:click={() =>
          hasPermission &&
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
          <RakennuksenNimi {energiatodistus} />
        </td>
        <td class="etp-table--td">
          <Address {energiatodistus} {postinumerot} />
        </td>
        <td class="etp-table--td">
          {Maybe.orSome('', energiatodistus['laatija-fullname'])}
        </td>
      </tr>
    </tbody>
  </table>
</div>
