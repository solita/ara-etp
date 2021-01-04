<script>
  import { tick } from 'svelte';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as deep from '@Utility/deep-objects';
  import { locale, _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import * as et from '@Component/Energiatodistus/energiatodistus-utils';

  export let disabled;
  export let schema;
  export let energiatodistus;

  export let kuormat = [];
  export let alakayttotarkoitusluokat = [];

  const findConstantKuorma = R.chain(
    id => Maybe.find(R.propEq('kayttotarkoitusluokka-id', id), kuormat));

  let constantKuorma = findConstantKuorma(et.findKayttotarkoitusluokkaId(
    energiatodistus.perustiedot.kayttotarkoitus,
    alakayttotarkoitusluokat
  ));

  $: if (!disabled) {

    let kayttotarkoitusluokkaId = et.findKayttotarkoitusluokkaId(
      energiatodistus.perustiedot.kayttotarkoitus,
      alakayttotarkoitusluokat
    );

    if (!R.equals(
          kayttotarkoitusluokkaId,
          R.map(R.prop('kayttotarkoitusluokka-id'), constantKuorma)))
    {
      constantKuorma = findConstantKuorma(kayttotarkoitusluokkaId);
      constantKuorma.forEach(kuorma => {
        tick().then(() => {
          energiatodistus = R.assocPath(['lahtotiedot', 'sis-kuorma'],
            deep.map(R.F, R.compose(Either.Right, Maybe.Some),
              R.dissoc('kayttotarkoitusluokka-id', kuorma)),
            energiatodistus);
        });
      });
    }
  }
</script>

<H3 text={$_('energiatodistus.lahtotiedot.sis-kuorma.header')} compact={true} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.sis-kuorma.kayttoaste')}
      </th>
      <th class="et-table--th">
        <span>{$_('energiatodistus.lahtotiedot.sis-kuorma.lampokuorma')}</span>
        <span class="block">W/m²</span>
      </th>
      <th class="et-table--th" />
      <th class="et-table--th" />
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['henkilot', 'kuluttajalaitteet', 'valaistus'] as sisKuorma}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.sis-kuorma.labels.${sisKuorma}`)}
        </td>
        <td class="et-table--td">
          <Input
            disabled = {disabled || constantKuorma.isSome()}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', sisKuorma, 'kayttoaste']} />
        </td>
        <td class="et-table--td">
          <Input
            disabled = {disabled ||
              (constantKuorma.isSome() && !R.equals(sisKuorma, 'valaistus'))}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'sis-kuorma', sisKuorma, 'lampokuorma']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
