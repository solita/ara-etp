<script>
  import * as R from 'ramda';
  import { locale, _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={$_('energiatodistus.lahtotiedot.sis-kuorma.header')} compact={true} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.sis-kuorma.kayttoaste')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.sis-kuorma.lampokuorma')}
      </th>
      <th class="et-table--th" />
      <th class="et-table--th" />
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['henkilot', 'kuluttajalaitteet', 'valaistus'] as sisKuorma}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.sis-kuorma.labels.${sisKuorma}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', sisKuorma, 'kayttoaste']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', sisKuorma, 'lampokuorma']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td">
          <!-- <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', index, 'valaistus']} /> -->
        </td>
      </tr>
    {/each}
  </tbody>
</table>
