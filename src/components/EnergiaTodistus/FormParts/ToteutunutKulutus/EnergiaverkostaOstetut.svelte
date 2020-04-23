<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3
  compact={true}
  text={$_('energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.header')} />

<table class="et-table mb-6">
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
    {#each ['kaukolampo', 'kokonaissahko', 'kiinteistosahko', 'kayttajasahko', 'kaukojaahdytys'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.labels.${energiamuoto}`)}
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
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
