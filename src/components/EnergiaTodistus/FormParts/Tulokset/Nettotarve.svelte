<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 compact={true} text={$_('energiatodistus.tulokset.nettotarve.header')} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.vuosikulutus')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.vuosikulutus-per-nelio')}
      </th>
      <th class="et-table--th" />
      <th class="et-table--th" />
    </tr>
  </thead>

  <tbody class="et-table--tbody">
    {#each ['tilojen-lammitys-vuosikulutus', 'ilmanvaihdon-lammitys-vuosikulutus', 'kayttoveden-valmistus-vuosikulutus', 'jaahdytys-vuosikulutus'] as tarve}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.tulokset.nettotarve.labels.${tarve}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'nettotarve', tarve]} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
