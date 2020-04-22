<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/EnergiaTodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text="Ilmanvaihtojärjestelmä" />

<div class="w-full py-4 mb-4">
  <Input
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'ilmanvaihto', 'kuvaus-fi']} />
</div>

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.ilmanvaihto.ilmavirta')}
        <br />
        {$_('energiatodistus.lahtotiedot.ilmanvaihto.tulo')}(m³/s) / {$_('energiatodistus.lahtotiedot.ilmanvaihto.poisto')}(m³/s)
      </th>
      <th class="et-table--th">
        Järjestelmän SFP-luku
        <br />
        kW/(m³/s)
      </th>
      <th class="et-table--th">LTO:n lämpötilasuhde</th>
      <th class="et-table--th">
        Jäätymisenesto
        <br />
        °C
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    <tr class="et-table--tr">
      <td class="et-table--td">Pääilmanvaihtokoneet</td>
      <td class="et-table--td flex flex-row items-end">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'tulo']} />
        /
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'poisto']} />
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'sfp']} />
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'lampotilasuhde']} />
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'jaatymisenesto']} />
      </td>
    </tr>
    {#each ['erillispoistot', 'ivjarjestelma'] as ilmanvaihto}
      <tr class="et-table--tr">
        <td class="et-table--td">{ilmanvaihto}</td>
        <td class="et-table--td flex flex-row items-end">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'tulo']} />
          /
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'poisto']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'sfp']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>

<div class="w-1/2 py-4 mb-4">
  <Input
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'ilmanvaihto', 'lto-vuosihyotysuhde']} />
</div>
