<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';

  import Checkbox from '@Component/Checkbox/Checkbox';
  import VuosiUnit from '@Pages/energiatodistus/form-parts/units/year';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';
  import Textarea from '@Pages/energiatodistus/Textarea';
  import ELukuUnit from '@Pages/energiatodistus/form-parts/units/e-luku';
  import VuosikulutusUnit from '@Pages/energiatodistus/form-parts/units/annual-energy';

  export let disabled;
  export let schema;
  export let energiatodistus;
  export let huomio;
  export let inputLanguage;
  export let versio = 2018;

  const base = `energiatodistus.huomiot.${huomio}`;
</script>

<H3 text={$_(`${base}.header`)} />

{#if R.and(R.equals(versio, 2026), R.includes(huomio, ['lammitys']))}
  <div class="my-4 flex flex-col gap-x-8">
    <div class="py-4 lg:w-2/5">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['huomiot', huomio, 'kayttoikaa-jaljella-arvio-vuosina']}
        unit={VuosiUnit} />
    </div>
  </div>
{/if}
<div class="mb-6 w-full">
  <Textarea
    {disabled}
    {schema}
    bind:model={energiatodistus}
    inputLanguage={Maybe.Some(inputLanguage)}
    path={['huomiot', huomio, 'teksti']} />
</div>

{#each R.path(['huomiot', huomio, 'toimenpide'], energiatodistus) as _, index}
  <div class="mb-6 w-full py-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      inputLanguage={Maybe.Some(inputLanguage)}
      path={['huomiot', huomio, 'toimenpide', index, 'nimi']} />
  </div>
{/each}

<div class="min-w-full overflow-x-auto">
  <table class="et-table mb-12">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th-left-aligned"
          >{$_('energiatodistus.huomiot.table-header')}</th>
        <th class="et-table--th et-table--th-right-aligned">
          <span>{$_('energiatodistus.huomiot.toimenpide-table.lampo')}</span>
          <span class="block"><VuosikulutusUnit /></span>
        </th>
        <th class="et-table--th et-table--th-right-aligned">
          <span>{$_('energiatodistus.huomiot.toimenpide-table.sahko')}</span>
          <span class="block"><VuosikulutusUnit /></span>
        </th>
        <th class="et-table--th et-table--th-right-aligned">
          <span
            >{$_('energiatodistus.huomiot.toimenpide-table.jaahdytys')}</span>
          <span class="block"><VuosikulutusUnit /></span>
        </th>
        <th class="et-table--th et-table--th-right-aligned">
          <span
            >{$_(
              'energiatodistus.huomiot.toimenpide-table.eluvun-muutos'
            )}</span>
          <span class="block"><ELukuUnit /></span>
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each R.path(['huomiot', huomio, 'toimenpide'], energiatodistus) as toimenpide, index}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {index + 1}. {$_(
              'energiatodistus.huomiot.toimenpide-table.ehdotus'
            )}
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['huomiot', huomio, 'toimenpide', index, 'lampo']} />
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['huomiot', huomio, 'toimenpide', index, 'sahko']} />
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['huomiot', huomio, 'toimenpide', index, 'jaahdytys']} />
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={[
                'huomiot',
                huomio,
                'toimenpide',
                index,
                'eluvun-muutos'
              ]} />
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
