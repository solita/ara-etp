<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import H4 from '@Component/H/H4';
  import Input from '@Pages/energiatodistus/Input';

  export let perusparannuspassi;
  export let schema;
  export let disabled = false;

  // Energy forms for the pricing table - mapped to tulokset field names
  const energiamuodot = [
    { key: 'kaukolampo', field: 'kaukolampo-hinta' },
    { key: 'sahko', field: 'sahko-hinta' },
    { key: 'uusiutuva-polttoaine', field: 'uusiutuvat-pat-hinta' },
    { key: 'fossiilinen-polttoaine', field: 'fossiiliset-pat-hinta' },
    { key: 'kaukojaahdytys', field: 'kaukojaahdytys-hinta' }
  ];
</script>

<H4 text={$_('perusparannuspassi.energian-hinta.header')} />
<p class="mb-6">{$_('perusparannuspassi.energian-hinta.description')}</p>

<div class="mb-12 min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder table-fixed border-r-0">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th
          class="et-table--th et-table--th-left-aligned et-table--th__twocells">
          {$_('perusparannuspassi.energian-hinta.energia')}
        </th>
        <th class="et-table--th et-table--th-right-aligned et-table--th__fifth">
          {$_('perusparannuspassi.energian-hinta.hinta-snt-kwh')}
        </th>
        <th class="et-table--th invisible w-3/5 border-0 p-0"></th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each energiamuodot as energiamuoto}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--th__twocells">
            {$_(`perusparannuspassi.energian-hinta.labels.${energiamuoto.key}`)}
          </td>
          <td class="et-table--td et-table--td__fifth">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={perusparannuspassi}
              path={['tulokset', energiamuoto.field]}
              i18nRoot="perusparannuspassi" />
          </td>
          <td class="et-table--td invisible w-3/5 border-0 p-0"></td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
