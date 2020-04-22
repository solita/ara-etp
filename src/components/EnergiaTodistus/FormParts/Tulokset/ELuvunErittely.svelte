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
  text={$_('energiatodistus.tulokset.kaytettavat-energiamuodot.header')} />

<table class="et-table et-table__noborder mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.kaytettavat-energiamuodot')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.vakioidun_kayton_ostoenergia')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.energiamuodon_kerroin')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.kertoimella_painotettu_energiankulutus')}
      </th>
      <th class="et-table--th" />
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['kaukolampo', 'sahko', 'uusiutuva-polttoaine', 'fossiilinen-polttoaine', 'kaukojaahdytys'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.tulokset.kaytettavat-energiamuodot.labels.${energiamuoto}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'kaytettavat-energiamuodot', energiamuoto]} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
    <tr class="et-table--tr border-t-1 border-disabled">
      <td class="et-table--td uppercase">
        {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.yhteensa')}
      </td>
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
    </tr>
  </tbody>
</table>
