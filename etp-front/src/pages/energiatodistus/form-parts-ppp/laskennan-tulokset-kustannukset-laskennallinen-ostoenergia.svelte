<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as EitherMaybe from '@Utility/either-maybe';
  import * as PppUtils from './ppp-utils';

  import H4 from '@Component/H/H4';
  import * as PPPUtils from '@Pages/energiatodistus/form-parts-ppp/ppp-utils.js';

  export let perusparannuspassi;
  export let energiatodistus;
  export let schema;
  export let disabled = false;

  $: costs = R.map(
    R.prop('laskennallinenKustannus'),
    PppUtils.calculateDerivedValues(energiatodistus, perusparannuspassi)
  );
</script>

<H4
  text={$_(
    'perusparannuspassi.laskennan-tulokset.kustannukset-laskennallinen.header'
  )} />
<p class="mb-6">
  {$_(
    'perusparannuspassi.laskennan-tulokset.kustannukset-laskennallinen.kuvaus'
  )}
</p>

<div class="mb-12 min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder table-fixed border-r-0">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th-left-aligned et-table--td__fifth">
          {$_(
            'perusparannuspassi.laskennan-tulokset.kustannukset-laskennallinen.energia'
          )}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--td__fifth">
          {$_(
            'perusparannuspassi.laskennan-tulokset.kustannukset-laskennallinen.lahtotilanne'
          )}
        </th>
        {#each perusparannuspassi.vaiheet as vaihe}
          <th class="et-table--th et-table--th-right-aligned"
            >{PPPUtils.formatVaiheHeading(
              `${$_('perusparannuspassi.laskennan-tulokset.vaihe')} ${vaihe['vaihe-nro']}`,
              $_('perusparannuspassi.laskennan-tulokset.eur-per-vuosi'),
              vaihe.tulokset['vaiheen-alku-pvm'],
              $_('perusparannuspassi.laskennan-tulokset.ei-aloitusvuotta')
            )}</th>
        {/each}
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each PppUtils.energiamuodot as { etEnergiamuoto }}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--td__fifth">
            {$_(
              `perusparannuspassi.laskennan-tulokset.kustannukset-laskennallinen.${etEnergiamuoto}`
            )}
          </td>

          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {PppUtils.formatCost(costs[0][etEnergiamuoto])}
          </td>

          {#each costs.slice(1) as vaiheCost}
            <td
              class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
              {#if EitherMaybe.isSome(vaiheCost['vaiheen-alku-pvm'])}
                {PppUtils.formatCost(vaiheCost[etEnergiamuoto])}
              {/if}
            </td>
          {/each}
        </tr>
      {/each}

      <!-- Yhteensä row -->
      <tr class="et-table--tr border-t-1 border-disabled">
        <td class="et-table--td et-table--td__fifth uppercase">
          {$_(
            'perusparannuspassi.laskennan-tulokset.kustannukset-laskennallinen.yhteensa'
          )}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          {PppUtils.formatCost(costs[0].total)}
        </td>
        {#each costs.slice(1) as vaiheCost}
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {#if EitherMaybe.isSome(vaiheCost['vaiheen-alku-pvm'])}
              {PppUtils.formatCost(vaiheCost.total)}
            {/if}
          </td>
        {/each}
      </tr>

      <!-- Erotus edelliseen vaiheeseen row -->
      <tr class="et-table--tr">
        <td class="et-table--td et-table--td__fifth">
          {$_(
            'perusparannuspassi.laskennan-tulokset.kustannukset-laskennallinen.erotus'
          )}
        </td>
        <td
          class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
          -
        </td>
        {#each R.zip(costs.slice(0, costs.length - 1), costs.slice(1, costs.length)) as [prev, cur]}
          <td
            class="et-table--td et-table--td__fifth border-l-1 border-disabled text-right">
            {#if EitherMaybe.isSome(cur['vaiheen-alku-pvm'])}
              {R.compose(PppUtils.formatCostDifference, R.lift(R.subtract))(
                cur.total,
                prev.total
              )}
            {/if}
          </td>
        {/each}
      </tr>
    </tbody>
  </table>
</div>
