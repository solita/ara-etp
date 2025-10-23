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

  // PoC: Mock vaiheet data until real implementation is merged
  // This simulates the structure from perusparannuspassi.vaiheet
  // Using similar progressive reduction pattern as laskennallinen-ostoenergia
  const mockVaiheet = [
    {
      'vaihe-nro': 1,
      tulokset: {
        'kaukolampo-vuosikulutus': Either.Right(12000),
        'kokonaissahko-vuosikulutus': Either.Right(9000),
        'uusiutuva-polttoaine': Either.Right(600),
        'kevyt-polttooljy': Either.Right(350),
        'kaukojaahdytys-vuosikulutus': Either.Right(250)
      }
    },
    {
      'vaihe-nro': 2,
      tulokset: {
        'kaukolampo-vuosikulutus': Either.Right(9500),
        'kokonaissahko-vuosikulutus': Either.Right(7000),
        'uusiutuva-polttoaine': Either.Right(500),
        'kevyt-polttooljy': Either.Right(250),
        'kaukojaahdytys-vuosikulutus': Either.Right(180)
      }
    },
    {
      'vaihe-nro': 3,
      tulokset: {
        'kaukolampo-vuosikulutus': Either.Right(7000),
        'kokonaissahko-vuosikulutus': Either.Right(5000),
        'uusiutuva-polttoaine': Either.Right(400),
        'kevyt-polttooljy': Either.Right(150),
        'kaukojaahdytys-vuosikulutus': Either.Right(120)
      }
    },
    {
      'vaihe-nro': 4,
      tulokset: {
        'kaukolampo-vuosikulutus': Either.Right(5000),
        'kokonaissahko-vuosikulutus': Either.Right(3000),
        'uusiutuva-polttoaine': Either.Right(300),
        'kevyt-polttooljy': Either.Right(80),
        'kaukojaahdytys-vuosikulutus': Either.Right(80)
      }
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
      const pilkkeetHavu = PppUtils.unwrapMaybe(
        muunnoskerrotutPolttoaineet['pilkkeet-havu-sekapuu']
      );
      const pilkkeetKoivu = PppUtils.unwrapMaybe(
        muunnoskerrotutPolttoaineet['pilkkeet-koivu']
      );
      const puupelletit = PppUtils.unwrapMaybe(
        muunnoskerrotutPolttoaineet['puupelletit']
      );

      const sum =
        (pilkkeetHavu || 0) + (pilkkeetKoivu || 0) + (puupelletit || 0);
      return sum > 0 ? sum : null;
    })();

    // Get fossiilinen polttoaine kWh value (kevyt-polttooljy)
    const kevytPolttooljy = PppUtils.unwrapMaybe(
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

  // Extract vaihe consumption values
  const vaiheConsumptionValues =
    PppUtils.extractVaiheConsumptionValues(mockVaiheet);

  function calculateCost(energiamuoto) {
    const consumption = consumptionValues[energiamuoto.consumptionField];
    const price = priceValues[energiamuoto.priceField];
    return PppUtils.calculateCostFromValues(consumption, price);
  }

  $: toteutuneetCosts =
    (ostettuEnergia || ostetutPolttoaineet) && tuloksetData
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

  $: toteutuneetTotalCost = EtUtils.sumEtValues(toteutuneetCosts);

  $: vaiheTotalCosts = vaiheCosts.map(EtUtils.sumEtValues);

  $: differences = PppUtils.calculateVaiheDifferences(
    vaiheTotalCosts,
    toteutuneetTotalCost
  );
</script>

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
            'perusparannuspassi.kustannukset-toteutunut-ostoenergia.erotus-edelliseen-vaiheeseen'
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
