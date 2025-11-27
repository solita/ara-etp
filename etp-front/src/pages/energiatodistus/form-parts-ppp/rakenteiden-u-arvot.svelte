<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n.js';
  import H3 from '@Component/H/H3.svelte';
  import H4 from '@Component/H/H4.svelte';
  import Input from '@Pages/energiatodistus/Input.svelte';
  import * as EitherMaybe from '@/utils/either-maybe.js';
  import Select from '@Component/Select/Select.svelte';

  export let schema;
  export let perusparannuspassi;
  export let energiatodistus;

  const rakenneKeyMap = {
    ulkoseinat: 'ulkoseinat',
    ylapohja: 'ylapohja',
    alapohja: 'alapohja',
    ikkunat: 'ikkunat',
    ulkoovet: 'ulkoovet'
  };
</script>

<H4
  text={$_(
    'perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.header'
  )} />

<div class="flex flex-col gap-x-8 lg:flex-row">
  <table class="et-table mb-12">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th
          class="et-table--th et-table--th__twocells et-table--th-left-aligned">
          {$_(
            'perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.rakenne'
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
      {#each ['ulkoseinat', 'ylapohja', 'alapohja', 'ikkunat', 'ulkoovet'] as rakenteet}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(
              `perusparannuspassi.rakennuksen-perustiedot.rakenteiden-u-arvot.${rakenteet}`
            )}
          </td>
          <td class="et-table--td">
            {R.compose(
              EitherMaybe.orSome('-'),
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
              required={true}
              i18nRoot="perusparannuspassi"
              path={['rakennuksen-perustiedot', `${rakenteet}-ehdotettu-taso`]}
            ></Input>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
