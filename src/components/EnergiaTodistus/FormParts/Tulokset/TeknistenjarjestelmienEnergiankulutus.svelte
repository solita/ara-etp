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
  text={$_('energiatodistus.tulokset.tekniset-jarjestelmat.header')} />

<table class="et-table et-table__noborder mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__twocells" />
      <th class="et-table--th">sahko</th>
      <th class="et-table--th">lampo</th>
      <th class="et-table--th">kaukojaahdytys kWh(m²vuosi)</th>
    </tr>
  </thead>

  <tbody class="et-table--tbody">
    {#each ['tilojen-lammitys', 'tuloilman-lammitys', 'kayttoveden-valmistus'] as jarjestelma}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.tulokset.tekniset-jarjestelmat.labels.${jarjestelma}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'tekniset-jarjestelmat', jarjestelma, 'sahko']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'tekniset-jarjestelmat', jarjestelma, 'lampo']} />
        </td>
        <td class="et-table--td" />
      </tr>
    {/each}
    <tr class="et-table--tr">
      <td class="et-table--td">
        {$_('energiatodistus.tulokset.tekniset-jarjestelmat.labels.iv-sahko')}
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['tulokset', 'tekniset-jarjestelmat', 'iv-sahko']} />
      </td>
      <td class="et-table--td" />
      <td class="et-table--td" />
    </tr>
    <tr class="et-table--tr">
      <td class="et-table--td">
        {$_('energiatodistus.tulokset.tekniset-jarjestelmat.labels.jaahdytys')}
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['tulokset', 'tekniset-jarjestelmat', 'jaahdytys', 'sahko']} />
      </td>
      <td class="et-table--td" />
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['tulokset', 'tekniset-jarjestelmat', 'jaahdytys', 'kaukojaahdytys']} />
      </td>
    </tr>
    <tr class="et-table--tr">
      <td class="et-table--td">
        {$_('energiatodistus.tulokset.tekniset-jarjestelmat.labels.kuluttajalaitteet-ja-valaistus-sahko')}
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['tulokset', 'tekniset-jarjestelmat', 'kuluttajalaitteet-ja-valaistus-sahko']} />
      </td>
      <td class="et-table--td" />
      <td class="et-table--td" />
    </tr>
    <tr class="et-table--tr border-t-1 border-disabled">
      <td class="et-table--td uppercase">
        {$_('energiatodistus.tulokset.yhteensa')}
      </td>
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
    </tr>
  </tbody>
</table>
