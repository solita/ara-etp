<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as PppUtils from './ppp-utils';

  import H4 from '@Component/H/H4';

  export let perusparannuspassi;
  export let energiatodistus;

  const toteutuneetOstoenergiat = [
    {
      pppEnergiamuoto: 'toteutunut-ostoenergia-kaukolampo',
      etEnergiamuoto: 'kaukolampo',
      getEnergiaFromEt: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kaukolampo-vuosikulutus'
        ],
      pppPriceField: 'kaukolampo-hinta'
    },
    {
      pppEnergiamuoto: 'toteutunut-ostoenergia-sahko',
      etEnergiamuoto: 'sahko',
      getEnergiaFromEt: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kokonaissahko-vuosikulutus'
        ],
      pppPriceField: 'sahko-hinta'
    },
    {
      pppEnergiamuoto: 'toteutunut-ostoenergia-uusiutuvat-pat',
      etEnergiamuoto: 'uusiutuva-polttoaine',
      getEnergiaFromEt: _ => Either.Right(Maybe.None()),
      pppPriceField: 'uusiutuvat-pat-hinta'
    },
    {
      pppEnergiamuoto: 'toteutunut-ostoenergia-fossiiliset-pat',
      etEnergiamuoto: 'fossiilinen-polttoaine',
      getEnergiaFromEt: _ => Either.Right(Maybe.None()),
      pppPriceField: 'fossiiliset-pat-hinta'
    },
    {
      pppEnergiamuoto: 'toteutunut-ostoenergia-kaukojaahdytys',
      etEnergiamuoto: 'kaukojaahdytys',
      getEnergiaFromEt: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kaukojaahdytys-vuosikulutus'
        ],
      pppPriceField: 'kaukojaahdytys-hinta'
    }
  ];

  const calculateCosts = (et, ppp) => {
    const multiplyEitherMaybe = (a, b) =>
      R.lift(R.multiply)(
        EtUtils.unnestValidation(a),
        EtUtils.unnestValidation(b)
      );

    const addTotal = costs =>
      R.assoc(
        'total',
        R.compose(
          R.reduce(
            (acc, cost) =>
              Maybe.isSome(acc) ? R.lift(R.add)(acc, cost) : cost,
            Maybe.None()
          ),
          R.filter(Maybe.isSome),
          R.values
        )(costs),
        costs
      );

    const etCosts = R.compose(
      addTotal,
      R.fromPairs,
      R.map(({ etEnergiamuoto, getEnergiaFromEt, pppPriceField }) => [
        etEnergiamuoto,
        multiplyEitherMaybe(getEnergiaFromEt(et), ppp.tulokset[pppPriceField])
      ])
    )(toteutuneetOstoenergiat);

    const pppCosts = R.map(
      vaihe =>
        R.compose(
          addTotal,
          R.fromPairs,
          R.map(({ etEnergiamuoto, pppEnergiamuoto, pppPriceField }) => [
            etEnergiamuoto,
            multiplyEitherMaybe(
              vaihe.tulokset[pppEnergiamuoto],
              ppp.tulokset[pppPriceField]
            )
          ])
        )(toteutuneetOstoenergiat),
      ppp.vaiheet
    );

    return [etCosts, ...pppCosts];
  };

  $: costs = calculateCosts(energiatodistus, perusparannuspassi);

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
      {#each toteutuneetOstoenergiat as { etEnergiamuoto }}
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
