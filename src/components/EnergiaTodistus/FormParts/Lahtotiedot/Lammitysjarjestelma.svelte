<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/EnergiaTodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={'Lämmitysjärjestelmä'} />

<div class="w-full py-4 mb-4">
  <Input
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'lammitys', 'kuvaus-fi']} />
</div>

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">Tuoton hyötysuhde</th>
      <th class="et-table--th">Jaon ja luovutuksen hyötysuhde</th>
      <th class="et-table--th">Lämpökerroin</th>
      <th class="et-table--th">
        Apulaitteiden sähkönkäyttö
        <br />
        kWh/(m²vuosi)
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['tilat-ja-iv', 'lammin-kayttovesi'] as hyotysuhde}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.lammitys.${hyotysuhde}.label`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'tuoton-hyotysuhde']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'jaon-hyotysuhde']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'lampokerroin']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'apulaitteet']} />
        </td>
      </tr>
    {/each}
  </tbody>
</table>

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">Kpl</th>
      <th class="et-table--th">Tuotto</th>
      <th class="et-table--th" />
      <th class="et-table--th" />
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['takka', 'ilmanlampopumppu'] as maaratuotto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.lammitys.${maaratuotto}.label`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', maaratuotto, 'maara']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', maaratuotto, 'tuotto']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
