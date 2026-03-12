<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EitherMaybe from '@Utility/either-maybe';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as formats from '@Utility/formats';
  import * as et from '@Pages/energiatodistus/energiatodistus-utils';

  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import Input from '@Pages/energiatodistus/Input';

  export let energiatodistus;
  export let schema;
  export let luokittelut;
  export let disabled = false;
  export let inputLanguage;

  $: labelLocale = LocaleUtils.label($locale);

  const sumFields = (energiatodistusData, section, subsection, fields) =>
    fields.reduce(
      (sum, field) =>
        sum +
        EitherMaybe.orSome(
          0,
          R.path(
            ['ilmastoselvitys', section, subsection, field],
            energiatodistusData
          )
        ),
      0
    );

  const jalanjalkiFields = [
    'rakennustuotteiden-valmistus',
    'kuljetukset-tyomaavaihe',
    'rakennustuotteiden-vaihdot',
    'energiankaytto',
    'purkuvaihe'
  ];

  const kadenjalkiFields = [
    'uudelleenkaytto',
    'kierratys',
    'ylimaarainen-uusiutuvaenergia',
    'hiilivarastovaikutus',
    'karbonatisoituminen'
  ];

  $: jalanjalkiRakennusTotal = sumFields(
    energiatodistus,
    'hiilijalanjalki',
    'rakennus',
    jalanjalkiFields
  );
  $: jalanjalkiRakennuspaikkaTotal = sumFields(
    energiatodistus,
    'hiilijalanjalki',
    'rakennuspaikka',
    jalanjalkiFields
  );
</script>

<style type="text/postcss">
  .et-table--thead .et-table--th {
    hyphens: auto;
    @apply overflow-hidden text-sm;
  }
</style>

<H2 id="ilmastoselvitys" text={$_('energiatodistus.ilmastoselvitys.header')} />

<div class="mb-8">
  <div class="flex flex-col gap-x-8 lg:flex-row">
    <div class="w-full py-4 lg:w-1/2">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['ilmastoselvitys', 'laatimisajankohta']} />
    </div>
    <div class="w-full py-4 lg:w-1/2">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['ilmastoselvitys', 'laatija']} />
    </div>
  </div>

  <div class="flex flex-col gap-x-8 lg:flex-row">
    <div class="w-full py-4 lg:w-1/2">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['ilmastoselvitys', 'yritys']} />
    </div>
    <div class="w-full py-4 lg:w-1/2">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['ilmastoselvitys', 'yritys-osoite']} />
    </div>
  </div>

  <div class="flex flex-col gap-x-8 lg:flex-row">
    <div class="w-full py-4 lg:w-1/4">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['ilmastoselvitys', 'yritys-postinumero']} />
    </div>
    <div class="w-full py-4 lg:w-1/4">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['ilmastoselvitys', 'yritys-postitoimipaikka']} />
    </div>
  </div>

  <div class="w-full py-4 lg:w-1/2">
    <Select
      id={'ilmastoselvitys.laadintaperuste'}
      name={'ilmastoselvitys.laadintaperuste'}
      label={$_('energiatodistus.ilmastoselvitys.laadintaperuste')}
      {disabled}
      bind:model={energiatodistus}
      lens={R.lensPath(['ilmastoselvitys', 'laadintaperuste'])}
      parse={Maybe.Some}
      format={et.selectFormat(
        labelLocale,
        luokittelut.ilmastoselvitysLaadintaperusteet
      )}
      items={R.pluck('id', luokittelut.ilmastoselvitysLaadintaperusteet)} />
  </div>
</div>

<div class="mb-8">
  <div class="min-w-full overflow-x-auto">
    <table class="et-table mb-6">
      <thead class="et-table--thead">
        <tr class="et-table--tr">
          <th
            class="et-table--th et-table--th__twocells et-table--th-left-aligned">
            {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.header')}
          </th>
          {#each jalanjalkiFields as field}
            <th class="et-table--th et-table--th-right-aligned">
              {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.' + field)}
            </th>
          {/each}
          <th class="et-table--th et-table--th-right-aligned">
            {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.yhteensa')}
          </th>
        </tr>
      </thead>
      <tbody class="et-table--tbody">
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus')}
          </td>
          {#each jalanjalkiFields as field}
            <td class="et-table--td">
              <Input
                {disabled}
                {schema}
                compact={true}
                center={true}
                bind:model={energiatodistus}
                path={[
                  'ilmastoselvitys',
                  'hiilijalanjalki',
                  'rakennus',
                  field
                ]} />
            </td>
          {/each}
          <td class="et-table--td">
            {formats.numberFormatPrecision(1, jalanjalkiRakennusTotal)}
          </td>
        </tr>
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(
              'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka'
            )}
          </td>
          {#each jalanjalkiFields as field}
            <td class="et-table--td">
              <Input
                {disabled}
                {schema}
                compact={true}
                center={true}
                bind:model={energiatodistus}
                path={[
                  'ilmastoselvitys',
                  'hiilijalanjalki',
                  'rakennuspaikka',
                  field
                ]} />
            </td>
          {/each}
          <td class="et-table--td">
            {formats.numberFormatPrecision(1, jalanjalkiRakennuspaikkaTotal)}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>

<div class="mb-8">
  <div class="min-w-full overflow-x-auto">
    <table class="et-table mb-6">
      <thead class="et-table--thead">
        <tr class="et-table--tr">
          <th
            class="et-table--th et-table--th__twocells et-table--th-left-aligned">
            {$_('energiatodistus.ilmastoselvitys.hiilikadenjalki.header')}
          </th>
          {#each kadenjalkiFields as field}
            <th class="et-table--th et-table--th-right-aligned">
              {$_('energiatodistus.ilmastoselvitys.hiilikadenjalki.' + field)}
            </th>
          {/each}
        </tr>
      </thead>
      <tbody class="et-table--tbody">
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus')}
          </td>
          {#each kadenjalkiFields as field}
            <td class="et-table--td">
              <Input
                {disabled}
                {schema}
                compact={true}
                center={true}
                bind:model={energiatodistus}
                path={[
                  'ilmastoselvitys',
                  'hiilikadenjalki',
                  'rakennus',
                  field
                ]} />
            </td>
          {/each}
        </tr>
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(
              'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka'
            )}
          </td>
          {#each kadenjalkiFields as field}
            <td class="et-table--td">
              <Input
                {disabled}
                {schema}
                compact={true}
                center={true}
                bind:model={energiatodistus}
                path={[
                  'ilmastoselvitys',
                  'hiilikadenjalki',
                  'rakennuspaikka',
                  field
                ]} />
            </td>
          {/each}
        </tr>
      </tbody>
    </table>
  </div>
</div>
