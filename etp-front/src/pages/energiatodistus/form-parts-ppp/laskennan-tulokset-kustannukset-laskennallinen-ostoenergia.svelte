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

  const laskennallisetOstoenergiat = [
    {
      pppEnergiamuoto: 'ostoenergian-tarve-kaukolampo',
      etEnergiamuoto: 'kaukolampo',
      pppPriceField: 'kaukolampo-hinta'
    },
    {
      pppEnergiamuoto: 'ostoenergian-tarve-sahko',
      etEnergiamuoto: 'sahko',
      pppPriceField: 'sahko-hinta'
    },
    {
      pppEnergiamuoto: 'ostoenergian-tarve-uusiutuvat-pat',
      etEnergiamuoto: 'uusiutuva-polttoaine',
      pppPriceField: 'uusiutuvat-pat-hinta'
    },
    {
      pppEnergiamuoto: 'ostoenergian-tarve-fossiiliset-pat',
      etEnergiamuoto: 'fossiilinen-polttoaine',
      pppPriceField: 'fossiiliset-pat-hinta'
    },
    {
      pppEnergiamuoto: 'ostoenergian-tarve-kaukojaahdytys',
      etEnergiamuoto: 'kaukojaahdytys',
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
      R.map(({ etEnergiamuoto, pppPriceField }) => [
        etEnergiamuoto,
        multiplyEitherMaybe(
          et.tulokset['kaytettavat-energiamuodot'][etEnergiamuoto],
          ppp.tulokset[pppPriceField]
        )
      ])
    )(laskennallisetOstoenergiat);

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
        )(laskennallisetOstoenergiat),
      ppp.vaiheet
    );

    return [etCosts, ...pppCosts];
  };

  $: costs = calculateCosts(energiatodistus, perusparannuspassi);
</script>

<H4 text={$_('perusparannuspassi.laskennallinen-ostoenergia.header')} />
<p class="mb-6">
  {$_('perusparannuspassi.laskennallinen-ostoenergia.description')}
</p>

<div class="mb-12 min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder table-fixed border-r-0">
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
      {#each laskennallisetOstoenergiat as { etEnergiamuoto }}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--td__fifth">
            {$_(
              `perusparannuspassi.laskennallinen-ostoenergia.labels.${etEnergiamuoto}`
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
          {$_('perusparannuspassi.laskennallinen-ostoenergia.yhteensa')}
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
            'perusparannuspassi.laskennallinen-ostoenergia.erotus-edelliseen-vaiheeseen'
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
