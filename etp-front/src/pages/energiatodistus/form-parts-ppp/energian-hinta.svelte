<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';

  export let perusparannuspassi;
  export let schema;
  export let disabled = false;

  // Energy forms for the pricing table
  const energiamuodot = [
    'kaukolampo',
    'sahko', 
    'uusiutuva-polttoaine',
    'fossiilinen-polttoaine',
    'kaukojaahdytys'
  ];

  // Helper function to capitalize first letter
  const capitalize = (str) => str.charAt(0).toUpperCase() + str.slice(1);
</script>

<H3 text={$_('perusparannuspassi.energian-hinta.header')} />

<div class="min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder border-r-0">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th__half et-table--th-left-aligned">
          {$_('perusparannuspassi.energian-hinta.energia')}
        </th>
        <th class="et-table--th et-table--th__half et-table--th-right-aligned">
          {$_('perusparannuspassi.energian-hinta.hinta-snt-kwh')}
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each energiamuodot as energiamuoto}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(`perusparannuspassi.energian-hinta.labels.${energiamuoto}`)}
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={perusparannuspassi}
              path={['energian-hinta', energiamuoto]}
              i18nRoot="perusparannuspassi" />
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>