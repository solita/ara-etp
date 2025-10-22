<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';

  import H3 from '@Component/H/H4';
  import Input from '@Pages/energiatodistus/Input';

  export let perusparannuspassi;
  export let energiatodistus;
  export let schema;
  export let disabled = false;

  const energiamuodot = [
    {
      key: 'kaukolampo',
      consumptionField: 'kaukolampo',
      priceField: 'kaukolampo-hinta'
    },
    {
      key: 'sahko',
      consumptionField: 'sahko',
      priceField: 'sahko-hinta'
    },
    {
      key: 'uusiutuva-polttoaine',
      consumptionField: 'uusiutuva-polttoaine',
      priceField: 'uusiutuvat-pat-hinta'
    },
    {
      key: 'fossiilinen-polttoaine',
      consumptionField: 'fossiilinen-polttoaine',
      priceField: 'fossiiliset-pat-hinta'
    },
    {
      key: 'kaukojaahdytys',
      consumptionField: 'kaukojaahdytys',
      priceField: 'kaukojaahdytys-hinta'
    }
  ];

  $: kaytettavatEnergiamuodot =
    energiatodistus?.tulokset?.['kaytettavat-energiamuodot'];

  $: tuloksetData = perusparannuspassi?.tulokset;

  $: consumptionValues = R.compose(
    R.map(EtUtils.unnestValidation),
    R.defaultTo({})
  )(kaytettavatEnergiamuodot);

  $: priceValues = R.compose(
    R.map(EtUtils.unnestValidation),
    R.defaultTo({})
  )(tuloksetData);

  function calculateCost(energiamuoto) {
    const consumption = Maybe.orSome(null, consumptionValues[energiamuoto.consumptionField]);
    const price = Maybe.orSome(null, priceValues[energiamuoto.priceField]);

    if (consumption === null || price === null) {
      return null;
    }

    const cost = (consumption * price) / 100;
    return cost;
  }

  $: lahtotilanneCosts = kaytettavatEnergiamuodot && tuloksetData ? energiamuodot.reduce((acc, energiamuoto) => {
    const cost = calculateCost(energiamuoto);
    acc[energiamuoto.key] = cost !== null ? cost.toFixed(2) : null;
    return acc;
  }, {}) : {};

  $: lahtotilanneTotalCost = (() => {
    const costs = Object.values(lahtotilanneCosts).filter(c => c !== null);
    if (costs.length === 0) return null;

    const total = costs.reduce((sum, cost) => sum + parseFloat(cost), 0);
    return total.toFixed(2);
  })();
</script>

<!-- TODO CHANGE TO h4 -->
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
      {#each energiamuodot as energiamuoto, i}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--td__fifth">
            {$_(
              `perusparannuspassi.laskennallinen-ostoenergia.labels.${energiamuoto.key}`
            )}
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {lahtotilanneCosts[energiamuoto.key] || '-'}
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            <!-- Vaihe 1 - calculated field, to be implemented -->
            -
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            <!-- Vaihe 2 - calculated field, to be implemented -->
            -
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            <!-- Vaihe 3 - calculated field, to be implemented -->
            -
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            <!-- Vaihe 4 - calculated field, to be implemented -->
            -
          </td>
        </tr>
      {/each}
      <!-- Yhteensä row -->
      <tr class="et-table--tr border-t-1 border-disabled">
        <td class="et-table--td et-table--td__fifth uppercase">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.yhteensa')}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          {lahtotilanneTotalCost || '-'}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          <!-- Vaihe 1 total - to be implemented -->
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          <!-- Vaihe 2 total - to be implemented -->
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          <!-- Vaihe 3 total - to be implemented -->
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          <!-- Vaihe 4 total - to be implemented -->
          -
        </td>
      </tr>
      <!-- Erotus edelliseen vaiheeseen row -->
      <tr class="et-table--tr">
        <td class="et-table--td et-table--td__fifth">
          {$_(
            'perusparannuspassi.laskennallinen-ostoenergia.erotus-edelliseen-vaiheeseen'
          )}
        </td>
        <td class="et-table--td et-table--td__fifth border-l-1 border-disabled">
          -
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
