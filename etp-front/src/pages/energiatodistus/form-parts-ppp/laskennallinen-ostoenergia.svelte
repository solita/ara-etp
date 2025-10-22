<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  import H4 from '@Component/H/H4';
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

  // PoC: Mock vaiheet data until real implementation is merged
  // This simulates the structure from perusparannuspassi.vaiheet
  const mockVaiheet = [
    {
      'vaihe-nro': 1,
      tulokset: {
        kaukolampo: Either.Right(10000),
        sahko: Either.Right(8000),
        'uusiutuva-polttoaine': Either.Right(500),
        'fossiilinen-polttoaine': Either.Right(300),
        kaukojaahdytys: Either.Right(200)
      }
    },
    {
      'vaihe-nro': 2,
      tulokset: {
        kaukolampo: Either.Right(8000),
        sahko: Either.Right(6000),
        'uusiutuva-polttoaine': Either.Right(400),
        'fossiilinen-polttoaine': Either.Right(200),
        kaukojaahdytys: Either.Right(150)
      }
    },
    {
      'vaihe-nro': 3,
      tulokset: {
        kaukolampo: Either.Right(6000),
        sahko: Either.Right(4000),
        'uusiutuva-polttoaine': Either.Right(300),
        'fossiilinen-polttoaine': Either.Right(100),
        kaukojaahdytys: Either.Right(100)
      }
    },
    {
      'vaihe-nro': 4,
      tulokset: {
        kaukolampo: Either.Right(4000),
        sahko: Either.Right(2000),
        'uusiutuva-polttoaine': Either.Right(200),
        'fossiilinen-polttoaine': Either.Right(50),
        kaukojaahdytys: Either.Right(50)
      }
    }
  ];

  $: kaytettavatEnergiamuodot =
    energiatodistus?.tulokset?.['kaytettavat-energiamuodot'];

  $: tuloksetData = perusparannuspassi?.tulokset;

  $: consumptionValues = R.compose(
    R.map(EtUtils.unnestValidation),
    R.defaultTo({})
  )(kaytettavatEnergiamuodot);

  $: priceValues = tuloksetData
    ? R.compose(R.map(EtUtils.unnestValidation), R.defaultTo({}))(tuloksetData)
    : {};

  $: vaiheConsumptionValues = mockVaiheet.map(vaihe =>
    R.compose(R.map(EtUtils.unnestValidation), R.defaultTo({}))(vaihe.tulokset)
  );

  // Helper function to unwrap Maybe values
  function unwrapMaybe(value) {
    return value && Maybe.isMaybe(value) ? Maybe.orSome(null, value) : value;
  }

  // Generic cost calculation function
  function calculateCostFromValues(consumption, price) {
    const consumptionValue = unwrapMaybe(consumption);
    const priceValue = unwrapMaybe(price);

    if (consumptionValue == null || priceValue == null) {
      return null;
    }

    return (consumptionValue * priceValue) / 100;
  }

  function calculateCost(energiamuoto) {
    const consumption = consumptionValues[energiamuoto.consumptionField];
    const price = priceValues[energiamuoto.priceField];
    return calculateCostFromValues(consumption, price);
  }

  function calculateVaiheCost(vaiheIndex, energiamuoto) {
    const consumption =
      vaiheConsumptionValues[vaiheIndex][energiamuoto.consumptionField];
    const price = priceValues[energiamuoto.priceField];
    return calculateCostFromValues(consumption, price);
  }

  // Helper function to calculate costs for all energy types
  function calculateAllCosts(calculateFn) {
    return energiamuodot.reduce((acc, energiamuoto) => {
      const cost = calculateFn(energiamuoto);
      acc[energiamuoto.key] = cost !== null ? Maybe.Some(cost) : Maybe.None();
      return acc;
    }, {});
  }

  $: lahtotilanneCosts =
    kaytettavatEnergiamuodot && tuloksetData
      ? calculateAllCosts(calculateCost)
      : {};

  // Calculate costs for each vaihe
  $: vaiheCosts = tuloksetData
    ? mockVaiheet.map((_, vaiheIndex) =>
        calculateAllCosts(energiamuoto =>
          calculateVaiheCost(vaiheIndex, energiamuoto)
        )
      )
    : [];

  $: lahtotilanneTotalCost = EtUtils.sumEtValues(lahtotilanneCosts);

  $: vaiheTotalCosts = vaiheCosts.map(EtUtils.sumEtValues);

  $: differences = vaiheTotalCosts.map((vaiheCost, index) => {
    const previousCost =
      index === 0 ? lahtotilanneTotalCost : vaiheTotalCosts[index - 1];

    return R.lift(R.subtract)(vaiheCost, previousCost);
  });
</script>

<!-- TODO CHANGE TO h4 -->
<H4 text={$_('perusparannuspassi.laskennallinen-ostoenergia.header')} />
<p class="mb-6">
  {$_('perusparannuspassi.laskennallinen-ostoenergia.description')}
</p>

<div class="min-w-full overflow-x-auto md:overflow-x-hidden mb-12">
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
            {R.compose(
              Maybe.orSome('-'),
              R.map(R.compose(formats.numberFormat, fxmath.round(2)))
            )(lahtotilanneCosts[energiamuoto.key])}
          </td>
          {#each vaiheCosts as vaiheEnergyCosts}
            <td
              class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
              {R.compose(
                Maybe.orSome('-'),
                R.map(R.compose(formats.numberFormat, fxmath.round(2)))
              )(vaiheEnergyCosts[energiamuoto.key])}
            </td>
          {/each}
        </tr>
      {/each}
      <!-- Yhteensä row -->
      <tr class="et-table--tr border-t-1 border-disabled">
        <td class="et-table--td et-table--td__fifth uppercase">
          {$_('perusparannuspassi.laskennallinen-ostoenergia.yhteensa')}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          {R.compose(
            Maybe.orSome('-'),
            R.map(R.compose(formats.numberFormat, fxmath.round(2)))
          )(lahtotilanneTotalCost)}
        </td>
        {#each vaiheTotalCosts as totalCost}
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {R.compose(
              Maybe.orSome('-'),
              R.map(R.compose(formats.numberFormat, fxmath.round(2)))
            )(totalCost)}
          </td>
        {/each}
      </tr>
      <!-- Erotus edelliseen vaiheeseen row -->
      <tr class="et-table--tr">
        <td class="et-table--td et-table--td__fifth">
          {$_(
            'perusparannuspassi.laskennallinen-ostoenergia.erotus-edelliseen-vaiheeseen'
          )}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        {#each differences as diff}
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {R.compose(
              Maybe.orSome('-'),
              R.map(value => {
                const formatted = R.compose(formats.numberFormat, fxmath.round(2))(value);
                return value < 0 ? formatted : `+${formatted}`;
              })
            )(diff)}
          </td>
        {/each}
      </tr>
    </tbody>
  </table>
</div>
