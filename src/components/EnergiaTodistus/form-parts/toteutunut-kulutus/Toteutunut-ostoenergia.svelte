<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3
  compact={true}
  text={$_('energiatodistus.toteutunut-ostoenergiankulutus.toteutuneet-yhteensa')} />

<table class="et-table et-table__noborder mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.vuosikulutus')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.vuosikulutus-per-nelio')}
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['sahko-vuosikulutus-yhteensa', 'kaukolampo-vuosikulutus-yhteensa', 'polttoaineet-vuosikulutus-yhteensa', 'kaukojaahdytys-vuosikulutus-yhteensa'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.toteutunut-ostoenergiankulutus.${energiamuoto}`)}
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td">
          {R.compose( Maybe.orSome(''), R.path([
              'toteutunut-ostoenergiankulutus',
              energiamuoto
            ]) )(energiatodistus)}
        </td>
        <td class="et-table--td" />
      </tr>
    {/each}
    <tr class="et-table--tr border-t-1 border-disabled">
      <td class="et-table--td uppercase">{$_('energiatodistus.yhteensa')}</td>
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
    </tr>
  </tbody>
</table>
