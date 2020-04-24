<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import Textarea from '@Component/Energiatodistus/Textarea';

  export let disabled;
  export let schema;
  export let energiatodistus;

  const base = 'energiatodistus.huomiot.ymparys';
</script>

<H3 text={$_(`${base}.header`)} compact={true} />

<div class="w-full py-4 mb-6">
  <Textarea
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['huomiot', 'ymparys', 'teksti-fi']} />
</div>

{#each R.path(['huomiot', 'ymparys', 'toimenpide'], energiatodistus) as _, index}
  <div class="w-full py-4 mb-6">
    <Input
      {disabled}
      {schema}
      bind:model={energiatodistus}
      path={['huomiot', 'ymparys', 'toimenpide', index, 'nimi-fi']} />
  </div>
{/each}

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        {$_('energiatodistus.huomiot.toimenpide-lampo')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.huomiot.toimenpide-sahko')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.huomiot.toimenpide-jaahdytys')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.huomiot.toimenpide-eluvun-muutos')}
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each R.path(['huomiot', 'ymparys', 'toimenpide'], energiatodistus) as toimenpide, index}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {index + 1}. {$_('energiatodistus.huomiot.toimenpide-ehdotus')}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['huomiot', 'ymparys', 'toimenpide', index, 'lampo']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['huomiot', 'ymparys', 'toimenpide', index, 'sahko']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['huomiot', 'ymparys', 'toimenpide', index, 'jaahdytys']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['huomiot', 'ymparys', 'toimenpide', index, 'eluvun-muutos']} />
        </td>
      </tr>
    {/each}
  </tbody>
</table>
