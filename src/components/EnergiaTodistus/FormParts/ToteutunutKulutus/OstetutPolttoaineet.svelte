<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;

  const muunnoskerroin = {
    'kevyt-polttooljy': 10,
    'pilkkeet-havu-sekapuu': 1300,
    'pilkkeet-koivu': 1700,
    puupelletit: 4.7
  };
</script>

<H3
  compact={true}
  text={$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.header')} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.polttoaine-vuodessa')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.yksikko')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.muunnoskerroin')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.vuosikulutus')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.vuosikulutus-per-nelio')}
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['kevyt-polttooljy', 'pilkkeet-havu-sekapuu', 'pilkkeet-koivu', 'puupelletit'] as polttoaine}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.labels.${polttoaine}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', polttoaine]} />
        </td>
        <td class="et-table--td">
          {$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.${polttoaine}-yksikko`)}
        </td>
        <td class="et-table--td">{muunnoskerroin[polttoaine]}</td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
    {#each R.path(['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'vapaa'], schema) as vapaa, index}
      <tr class="et-table--tr">
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'vapaa', index, 'nimi']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'vapaa', index, 'maara-vuodessa']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'vapaa', index, 'yksikko']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'vapaa', index, 'muunnoskerroin']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
