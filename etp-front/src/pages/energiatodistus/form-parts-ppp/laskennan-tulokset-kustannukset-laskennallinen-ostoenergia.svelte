<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as PppUtils from './ppp-utils';
  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  import H4 from '@Component/H/H4';

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
        kaukolampo: Either.Right(Maybe.Some(10000)),
        sahko: Either.Right(Maybe.Some(8000)),
        'uusiutuva-polttoaine': Either.Right(Maybe.Some(500)),
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(300)),
        kaukojaahdytys: Either.Right(Maybe.Some(200))
      }
    },
    {
      'vaihe-nro': 2,
      tulokset: {
        kaukolampo: Either.Right(Maybe.Some(8000)),
        sahko: Either.Right(Maybe.Some(6000)),
        'uusiutuva-polttoaine': Either.Right(Maybe.Some(400)),
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(200)),
        kaukojaahdytys: Either.Right(Maybe.Some(150))
      }
    },
    {
      'vaihe-nro': 3,
      tulokset: {
        kaukolampo: Either.Right(Maybe.Some(6000)),
        sahko: Either.Right(Maybe.Some(4000)),
        'uusiutuva-polttoaine': Either.Right(Maybe.Some(300)),
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(100)),
        kaukojaahdytys: Either.Right(Maybe.Some(100))
      }
    },
    {
      'vaihe-nro': 4,
      tulokset: {
        kaukolampo: Either.Right(Maybe.Some(4000)),
        sahko: Either.Right(Maybe.Some(2000)),
        'uusiutuva-polttoaine': Either.Right(Maybe.Some(200)),
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(50)),
        kaukojaahdytys: Either.Right(Maybe.Some(50))
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

  const vaiheConsumptionValues =
    PppUtils.extractVaiheConsumptionValues(mockVaiheet);

  function calculateCost(energiamuoto) {
    const consumption = consumptionValues[energiamuoto.consumptionField];
    const price = priceValues[energiamuoto.priceField];
    return PppUtils.calculateCostFromValues(consumption, price);
  }

  $: lahtotilanneCosts =
    kaytettavatEnergiamuodot && tuloksetData
      ? PppUtils.calculateAllCosts(energiamuodot, calculateCost)
      : {};

  // Calculate costs for each vaihe
  $: vaiheCosts = tuloksetData
    ? PppUtils.calculateVaiheCosts(
        mockVaiheet,
        energiamuodot,
        vaiheConsumptionValues,
        priceValues
      )
    : [];

  $: lahtotilanneTotalCost = EtUtils.sumEtValues(lahtotilanneCosts);

  $: vaiheTotalCosts = vaiheCosts.map(EtUtils.sumEtValues);

  $: differences = PppUtils.calculateVaiheDifferences(
    vaiheTotalCosts,
    lahtotilanneTotalCost
  );
</script>

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
                const formatted = R.compose(
                  formats.numberFormat,
                  fxmath.round(2)
                )(value);
                return value < 0 ? formatted : `+${formatted}`;
              })
            )(diff)}
          </td>
        {/each}
      </tr>
    </tbody>
  </table>
</div>
