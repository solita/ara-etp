<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n.js';
  import H3 from '@Component/H/H3.svelte';
  import H4 from '@Component/H/H4.svelte';
  import Input from '@Pages/energiatodistus/Input.svelte';
  import { Maybe } from '@Utility/maybe-utils';

  export let schema;
  export let perusparannuspassi;
  export let energiatodistus;

  const rakenneKeyMap = {
    'ulkoseinat-ehdotettu-taso': 'ulkoseinat',
    'ylapohja-ehdotettu-taso': 'ylapohja',
    'alapohja-ehdotettu-taso': 'alapohja',
    'ikkunat-ehdotettu-taso': 'ikkunat',
    'ulkoovet-ehdotettu-taso': 'ulkoovet'
  };

  function unwrapMonetValue(monetVal) {
    return monetVal?.isRightValue && monetVal.value?.isValue
      ? monetVal.value.val
      : null;
  }
</script>

<H3 text={$_('perusparannuspassi.rakennuksen-perustiedot.header')} />
<div class="py-4">
  <H4
    text={$_(
      'perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.header'
    )} />
</div>

<div class="flex lg:flex-row flex-col gap-x-8">
  <table class="et-table mb-12">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th
          class="et-table--th et-table--th__twocells et-table--th-left-aligned">
          {$_(
            `perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.title`
          )}
        </th>
        <th
          class="et-table--th et-table--th-right-aligned et-table--th__twocells">
          {$_(
            'perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.lahtotiedot'
          )}
          <span class="block">W/(m²K)</span>
        </th>
        <th
          class="et-table--th et-table--th-right-aligned et-table--th__twocells">
          {$_(
            'perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.viimeisen-vaiheen-jalkeen'
          )}
          <span class="block">W/(m²K)</span>
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each ['ulkoseinat-ehdotettu-taso', 'ylapohja-ehdotettu-taso', 'alapohja-ehdotettu-taso', 'ikkunat-ehdotettu-taso', 'ulkoovet-ehdotettu-taso'] as rakenteet}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(
              `perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.labels.${rakenteet}`
            )}
          </td>
          <td class="et-table--td">
            {R.compose(
              m => m.orSome('–'),
              Maybe.fromNull,
              unwrapMonetValue,
              R.path([
                'lahtotiedot',
                'rakennusvaippa',
                rakenneKeyMap[rakenteet],
                'U'
              ])
            )(energiatodistus)}
          </td>
          <td>
            <Input
              {schema}
              bind:model={perusparannuspassi}
              compact={true}
              i18nRoot="perusparannuspassi"
              path={['rakennuksen-perustiedot', rakenteet]}></Input>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
