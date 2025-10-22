<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  import H4 from '@Component/H/H4';

  export let perusparannuspassi;
  export let energiatodistus;

  const energiamuodot = [
    {
      key: 'kaukolampo',
      consumptionField: 'kaukolampo-vuosikulutus',
      priceField: 'kaukolampo-hinta'
    },
    {
      key: 'sahko',
      consumptionField: 'kokonaissahko-vuosikulutus',
      priceField: 'sahko-hinta'
    },
    {
      key: 'uusiutuva-polttoaine',
      consumptionField: 'uusiutuva-polttoaine', // computed from multiple fields
      priceField: 'uusiutuvat-pat-hinta'
    },
    {
      key: 'fossiilinen-polttoaine',
      consumptionField: 'kevyt-polttooljy',
      priceField: 'fossiiliset-pat-hinta'
    },
    {
      key: 'kaukojaahdytys',
      consumptionField: 'kaukojaahdytys-vuosikulutus',
      priceField: 'kaukojaahdytys-hinta'
    }
  ];

  $: tuloksetData = perusparannuspassi?.tulokset;

  $: ostettuEnergia =
    energiatodistus?.['toteutunut-ostoenergiankulutus']?.['ostettu-energia'];

  $: ostetutPolttoaineet =
    energiatodistus?.['toteutunut-ostoenergiankulutus']?.[
      'ostetut-polttoaineet'
    ];

  // Conversion factors from ostetut-polttoaineet component
  const muunnoskertoimet = {
    'kevyt-polttooljy': Maybe.Some(10),
    'pilkkeet-havu-sekapuu': Maybe.Some(1300),
    'pilkkeet-koivu': Maybe.Some(1700),
    puupelletit: Maybe.Some(4.7)
  };

  // Calculate kWh values by multiplying amounts with conversion factors
  $: muunnoskerrotutPolttoaineet = R.compose(
    R.map(R.apply(EtUtils.multiplyWithKerroin)),
    R.mergeWith(Array.of, muunnoskertoimet),
    EtUtils.polttoaineet
  )(energiatodistus);

  // Extract and unnest consumed energy values
  $: consumptionValues = (() => {
    if (!ostettuEnergia && !ostetutPolttoaineet) return {};

    const ostettuEnergiaValues = ostettuEnergia
      ? R.compose(
          R.map(EtUtils.unnestValidation),
          R.defaultTo({})
        )(ostettuEnergia)
      : {};

    // Calculate sum of uusiutuva polttoaine kWh values
    const uusiutuvaPolttoaine = (() => {
      const pilkkeetHavu = unwrapMaybe(
        muunnoskerrotutPolttoaineet['pilkkeet-havu-sekapuu']
      );
      const pilkkeetKoivu = unwrapMaybe(
        muunnoskerrotutPolttoaineet['pilkkeet-koivu']
      );
      const puupelletit = unwrapMaybe(
        muunnoskerrotutPolttoaineet['puupelletit']
      );

      const sum =
        (pilkkeetHavu || 0) + (pilkkeetKoivu || 0) + (puupelletit || 0);
      return sum > 0 ? sum : null;
    })();

    // Get fossiilinen polttoaine kWh value (kevyt-polttooljy)
    const kevytPolttooljy = unwrapMaybe(
      muunnoskerrotutPolttoaineet['kevyt-polttooljy']
    );

    return {
      ...ostettuEnergiaValues,
      'uusiutuva-polttoaine': uusiutuvaPolttoaine,
      'kevyt-polttooljy': kevytPolttooljy
    };
  })();

  $: priceValues = tuloksetData
    ? R.compose(R.map(EtUtils.unnestValidation), R.defaultTo({}))(tuloksetData)
    : {};

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

  // Helper function to calculate costs for all energy types
  function calculateAllCosts(calculateFn) {
    return energiamuodot.reduce((acc, energiamuoto) => {
      const cost = calculateFn(energiamuoto);
      acc[energiamuoto.key] = cost !== null ? Maybe.Some(cost) : Maybe.None();
      return acc;
    }, {});
  }

  $: toteutuneetCosts =
    (ostettuEnergia || ostetutPolttoaineet) && tuloksetData
      ? calculateAllCosts(calculateCost)
      : {};

  $: toteutuneetTotalCost = EtUtils.sumEtValues(toteutuneetCosts);
</script>

<!-- TODO CHANGE TO h4 -->
<H4
  text={$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.header')} />
<p class="mb-6">
  {$_('perusparannuspassi.kustannukset-toteutunut-ostoenergia.description')}
</p>

<div class="min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder border-r-0 table-fixed">
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
      {#each energiamuodot as energiamuoto}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--td__fifth">
            {$_(
              `perusparannuspassi.kustannukset-toteutunut-ostoenergia.labels.${energiamuoto.key}`
            )}
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {R.compose(
              Maybe.orSome('-'),
              R.map(R.compose(formats.numberFormat, fxmath.round(2)))
            )(toteutuneetCosts[energiamuoto.key])}
          </td>
          <!-- Placeholder for vaihe columns -->
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            -
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            -
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            -
          </td>
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            -
          </td>
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
          {R.compose(
            Maybe.orSome('-'),
            R.map(R.compose(formats.numberFormat, fxmath.round(2)))
          )(toteutuneetTotalCost)}
        </td>
        <!-- Placeholder for vaihe totals -->
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
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
        <!-- Placeholder for vaihe differences -->
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
      </tr>
    </tbody>
  </table>
</div>
