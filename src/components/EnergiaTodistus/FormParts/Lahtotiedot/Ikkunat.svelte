<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/EnergiaTodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 compact={true} text="Ikkunat ilmansuunnittain" />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">Ala (m²)</th>
      <th class="et-table--th">U (W/(m²K))</th>
      <th class="et-table--th">g-kohtisuora</th>
      <th class="et-table--th" />
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['pohjoinen', 'koillinen', 'ita', 'kaakko', 'etela', 'lounas', 'lansi', 'luode'] as ikkuna}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.ikkunat.${ikkuna}.label`)}
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
