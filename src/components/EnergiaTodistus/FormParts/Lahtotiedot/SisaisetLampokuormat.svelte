<script>
  import * as R from 'ramda';
  import { locale, _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/EnergiaTodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={'Sisäiset lämpökuormat eri käyttöasteilla'} compact={true} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">Käyttöaste</th>
      <th class="et-table--th">
        Henkilöt
        <br />
        W/m²
      </th>
      <th class="et-table--th">
        Kuluttajalaitteet
        <br />
        W/m²
      </th>
      <th class="et-table--th">
        Valaistus
        <br />
        W/m²
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each R.path(['lahtotiedot', 'sis-kuorma'], energiatodistus) as sisKuorma, index}
      <tr class="et-table--tr">
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', index, 'selite-fi']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', index, 'kayttoaste']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', index, 'henkilot']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', index, 'kuluttajalaitteet']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', index, 'valaistus']} />
        </td>
      </tr>
    {/each}
  </tbody>
</table>
