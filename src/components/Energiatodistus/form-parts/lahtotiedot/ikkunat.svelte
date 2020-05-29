<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 compact={true} text={$_('energiatodistus.lahtotiedot.ikkunat.header')} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        <span>{$_('energiatodistus.lahtotiedot.ala')}</span>
        <span class="block">m²</span>
      </th>
      <th class="et-table--th">
        <span>{$_('energiatodistus.lahtotiedot.U')}</span>
        <span class="block">W/(m²K)</span>
      </th>
      <th class="et-table--th">
        g<sub>{$_('energiatodistus.lahtotiedot.ikkunat.kohtisuora')}</sub>
      </th>
      <th class="et-table--th" />
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['pohjoinen', 'koillinen', 'ita', 'kaakko', 'etela', 'lounas', 'lansi', 'luode'] as ikkuna}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.ikkunat.labels.${ikkuna}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ikkunat', ikkuna, 'ala']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ikkunat', ikkuna, 'U']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ikkunat', ikkuna, 'g-ks']} />
        </td>
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
