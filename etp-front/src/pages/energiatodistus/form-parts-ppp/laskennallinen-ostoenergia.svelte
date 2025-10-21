<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';

  export let perusparannuspassi;
  export let schema;
  export let disabled = false;

  // Energy forms for the table rows
  const energiamuodot = [
    'kaukolampo',
    'sahko',
    'uusiutuva-polttoaine',
    'fossiilinen-polttoaine',
    'kaukojaahdytys'
  ];
</script>

<H3 text={$_('perusparannuspassi.laskennallinen-ostoenergia.header')} />
<p class="mb-6">
  {$_('perusparannuspassi.laskennallinen-ostoenergia.description')}
</p>

<div class="min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder border-r-0 table-fixed">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th-left-aligned et-table--td__fifth">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.energia')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.lahtotilanne')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.vaihe-1')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.vaihe-2')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.vaihe-3')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.vaihe-4')}
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each energiamuodot as energiamuoto}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--td__fifth">
            {$_(`perusparannuspassi.laskennallinen-ostoenergia.labels.${energiamuoto}`)}
          </td>
          <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
            <!-- Lähtötilanne - calculated field -->
          </td>
          <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
            <!-- Vaihe 1 - calculated field -->
          </td>
          <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
            <!-- Vaihe 2 - calculated field -->
          </td>
          <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
            <!-- Vaihe 3 - calculated field -->
          </td>
          <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
            <!-- Vaihe 4 - calculated field -->
          </td>
        </tr>
      {/each}
      <!-- Yhteensä row -->
      <tr class="et-table--tr border-t-1 border-disabled">
        <td class="et-table--td et-table--td__fifth uppercase">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.yhteensa')}
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Total - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Total - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Total - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Total - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Total - calculated field -->
        </td>
      </tr>
      <!-- Erotus edelliseen vaiheeseen row -->
      <tr class="et-table--tr">
        <td class="et-table--td et-table--td__fifth">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.erotus-edelliseen-vaiheeseen')}
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Difference - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Difference - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Difference - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Difference - calculated field -->
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          <!-- Difference - calculated field -->
        </td>
      </tr>
    </tbody>
  </table>
</div>
