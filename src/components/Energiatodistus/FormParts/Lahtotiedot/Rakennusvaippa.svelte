<script>
  import { _ } from '@Language/i18n';
  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={$_('energiatodistus.lahtotiedot.rakennusvaippa.header')} />

<div class="w-1/5 py-4 mb-6">
  <Input
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'rakennusvaippa', 'ilmanvuotoluku']} />
</div>

<div class="min-w-full overflow-x-auto">
  <table class="et-table mb-6">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th" />
        <th class="et-table--th">{$_('energiatodistus.lahtotiedot.ala')}</th>
        <th class="et-table--th">{$_('energiatodistus.lahtotiedot.U')}</th>
        <th class="et-table--th">
          {$_('energiatodistus.lahtotiedot.rakennusvaippa.U*A')}
        </th>
        <th class="et-table--th">
          {$_('energiatodistus.lahtotiedot.rakennusvaippa.osuuslampohaviosta')}
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each ['ulkoseinat', 'ylapohja', 'alapohja', 'ikkunat', 'ulkoovet'] as vaippa}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(`energiatodistus.lahtotiedot.rakennusvaippa.labels.${vaippa}`)}
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['lahtotiedot', 'rakennusvaippa', vaippa, 'ala']} />
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['lahtotiedot', 'rakennusvaippa', vaippa, 'U']} />
          </td>
          <td class="et-table--td" />
          <td class="et-table--td" />
        </tr>
      {/each}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_('energiatodistus.lahtotiedot.rakennusvaippa.kylmasillat')}
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'rakennusvaippa', 'kylmasillat-UA']} />
        </td>
        <td class="et-table--td" />
      </tr>
    </tbody>
  </table>
</div>
