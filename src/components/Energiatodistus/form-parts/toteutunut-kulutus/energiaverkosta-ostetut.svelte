<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';
  import * as formats from '@Utility/formats';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import VuosikulutusPerAlaUnit from '@Component/Energiatodistus/form-parts/units/annual-energy-over-area';
  import VuosikulutusUnit from '@Component/Energiatodistus/form-parts/units/annual-energy';

  export let disabled;
  export let schema;
  export let energiatodistus;
  export let versio;
</script>

<style type="text/postcss">
  .indent {
    @apply pl-4;
  }
</style>

<H3
  compact={true}
  text={$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.header.${versio}`)} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth">
        <VuosikulutusUnit />
      </th>
      <th class="et-table--th et-table--th__sixth">
        <VuosikulutusPerAlaUnit />
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['kaukolampo', 'kokonaissahko', 'kiinteistosahko', 'kayttajasahko', 'kaukojaahdytys'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          <div
            class:indent={R.includes(energiamuoto, [
              'kiinteistosahko',
              'kayttajasahko'
            ])}>
            {$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.labels.${energiamuoto}`)}
          </div>
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostettu-energia', `${energiamuoto}-vuosikulutus`]} />
        </td>
        <td class="et-table--td">
          {formats.optionalNumber(EtUtils.energiaPerLammitettyNettoala(
            ['toteutunut-ostoenergiankulutus', 'ostettu-energia', `${energiamuoto}-vuosikulutus`])
            (energiatodistus))}
        </td>
      </tr>
    {/each}
    {#each R.defaultTo([], energiatodistus['toteutunut-ostoenergiankulutus']['ostettu-energia'].muu) as _, index}
      <tr class="et-table--tr">
        <td class="et-table--td">
          <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['toteutunut-ostoenergiankulutus', 'ostettu-energia', 'muu', index, 'nimi-fi']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td">
          <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['toteutunut-ostoenergiankulutus', 'ostettu-energia', 'muu', index, 'vuosikulutus']} />
        </td>
        <td class="et-table--td">
          {formats.optionalNumber(EtUtils.energiaPerLammitettyNettoala(
            ['toteutunut-ostoenergiankulutus', 'ostettu-energia', 'muu', index, 'vuosikulutus'])
            (energiatodistus))}
        </td>
      </tr>
    {/each}
  </tbody>
</table>
