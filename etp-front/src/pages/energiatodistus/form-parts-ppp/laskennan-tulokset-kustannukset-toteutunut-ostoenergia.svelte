<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as PppUtils from './ppp-utils';

  import H4 from '@Component/H/H4';

  export let perusparannuspassi;
  export let energiatodistus;

  $: costs = R.map(
    R.prop('toteutunutKustannus'),
    PppUtils.calculateDerivedValues(energiatodistus, perusparannuspassi)
  );
</script>

<H4
  text={$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.header')} />
<p class="mb-6">
  {$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.description')}
</p>

<div class="min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder table-fixed border-r-0">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th-left-aligned et-table--td__fifth">
          {$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.energia')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_(
            'perusparannuspassi.kustannukset-toteutunut-ostoenergia.lahtotilanne'
          )}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.vaihe-1')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.vaihe-2')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.vaihe-3')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.vaihe-4')}
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each PppUtils.energiamuodot as { etEnergiamuoto }}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--td__fifth">
            {$_(
              `perusparannuspassi.kustannukset-toteutunut-ostoenergia.labels.${etEnergiamuoto}`
            )}
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {PppUtils.formatCost(costs[0][etEnergiamuoto])}
          </td>
          {#each costs.slice(1) as vaiheCost}
            <td
              class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
              {PppUtils.formatCost(vaiheCost[etEnergiamuoto])}
            </td>
          {/each}
        </tr>
      {/each}
      <!-- Yhteensä row -->
      <tr class="et-table--tr border-t-1 border-disabled">
        <td class="et-table--td et-table--td__fifth uppercase">
          {$_(
            'perusparannuspassi.kustannukset-toteutunut-ostoenergia.yhteensa'
          )}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          {PppUtils.formatCost(costs[0].total)}
        </td>
        {#each costs.slice(1) as vaiheCost}
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {PppUtils.formatCost(vaiheCost.total)}
          </td>
        {/each}
      </tr>
      <!-- Erotus edelliseen vaiheeseen row -->
      <tr class="et-table--tr">
        <td class="et-table--td et-table--td__fifth">
          {$_(
            'perusparannuspassi.kustannukset-toteutunut-ostoenergia.erotus-edelliseen-vaiheeseen'
          )}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        {#each R.zip(costs.slice(0, costs.length - 1), costs.slice(1, costs.length)) as [prev, cur]}
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {R.compose(PppUtils.formatCostDifference, R.lift(R.subtract))(
              cur.total,
              prev.total
            )}
          </td>
        {/each}
      </tr>
    </tbody>
  </table>
</div>
