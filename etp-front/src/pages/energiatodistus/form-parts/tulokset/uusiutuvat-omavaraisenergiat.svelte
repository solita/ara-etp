<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';
  import VuosituottoUnit from '@Pages/energiatodistus/form-parts/units/annual-energy';
  import VuosituottoAreaUnit from '@Pages/energiatodistus/form-parts/units/annual-energy-over-area.svelte';

  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  export let disabled;
  export let schema;
  export let energiatodistus;
  export let versio = 2018;

  $: omavaraisenergiatPerLammitettyNettoala = R.compose(
    EtUtils.perLammitettyNettoala(energiatodistus),
    EtUtils.omavaraisenergiat
  )(energiatodistus);
</script>

<H3
  text={$_(
    'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.header.' + versio
  )} />

<div class="border-right-0 min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table mb-12">
    {#if R.equals(versio, 2026)}
      <colgroup>
        <col class="w-1/4" />
        <col class="w-1/4" />
        <col class="w-1/4" />
        <col class="w-1/4" />
      </colgroup>
      <thead class="et-table--thead">
        <tr class="et-table--tr">
          <th
            scope="col"
            rowspan="2"
            class="et-table--th et-table--th-left-aligned align-top">
            {$_(
              'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.table-header'
            )}
          </th>

          <th
            scope="colgroup"
            colspan="2"
            class="et-table--th et-table--th-group et-table--th-left-aligned">
            {$_(
              'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.table-rakennuksen-hyodyntama-osuus-header'
            )}
          </th>

          <th scope="col" class="et-table--th et-table--th-left-aligned">
            {$_(
              'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.table-kokonaistuotto-header'
            )}
          </th>
        </tr>

        <tr class="et-table--tr">
          <th
            scope="col"
            class="et-table--th et-table--th-left-aligned et-table--with-left-border">
            <VuosituottoUnit />
          </th>
          <th
            scope="col"
            class="et-table--th et-table--th-left-aligned et-table--no-left-border">
            <VuosituottoAreaUnit />
          </th>

          <th scope="col" class="et-table--th et-table--th-left-aligned">
            <VuosituottoUnit />
          </th>
        </tr>
      </thead>
    {:else}
      <thead class="et-table--thead">
        <tr class="et-table--tr">
          <th
            class="et-table--th et-table--th__twocells et-table--th-left-aligned">
            {$_(
              `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.table-header`
            )}
          </th>
          <th
            class="et-table--th et-table--th-right-aligned et-table--th__twocells">
            <VuosituottoUnit />
          </th>
          <th
            class="et-table--th et-table--th-right-aligned et-table--th__twocells">
            <VuosituottoAreaUnit />
          </th>
        </tr>
      </thead>
    {/if}

    <tbody class="et-table--tbody">
      {#each ['aurinkosahko', 'aurinkolampo', 'tuulisahko', 'lampopumppu', 'muusahko', 'muulampo'] as energiamuoto}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(
              `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.labels.${energiamuoto}`
            )}
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={[
                'tulokset',
                'uusiutuvat-omavaraisenergiat',
                energiamuoto
              ]} />
          </td>
          <td class="et-table--td">
            {R.compose(
              Maybe.orSome(''),
              R.map(R.compose(formats.numberFormat, fxmath.round(0))),
              R.prop(energiamuoto)
            )(omavaraisenergiatPerLammitettyNettoala)}
          </td>
          {#if R.equals(versio, 2026)}
            <td class="et-table--td">
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={[
                  'tulokset',
                  'uusiutuvat-omavaraisenergiat',
                  'kokonaistuotanto',
                  energiamuoto
                ]} />
            </td>
          {/if}
        </tr>
      {/each}
    </tbody>
  </table>
</div>
