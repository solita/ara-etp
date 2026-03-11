<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as LocaleUtils from '@Language/locale-utils';
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

  const extractNumericValue = value => {
    if (Either.isEither(value) && Either.isRight(value)) {
      const inner = Either.right(value);
      if (Maybe.isMaybe(inner) && Maybe.isSome(inner)) {
        return Maybe.get(inner);
      }
    }
    return 0;
  };

  const sumFields = (section, subsection, fields) =>
    fields.reduce(
      (sum, field) =>
        sum +
        extractNumericValue(
          R.path(
            ['ilmastoselvitys', section, subsection, field],
            energiatodistus
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
    'hiilijalanjalki',
    'rakennus',
    jalanjalkiFields
  );
  $: jalanjalkiRakennuspaikkaTotal = sumFields(
    'hiilijalanjalki',
    'rakennuspaikka',
    jalanjalkiFields
  );
  $: kadenjalkiRakennusTotal = sumFields(
    'hiilikadenjalki',
    'rakennus',
    kadenjalkiFields
  );
  $: kadenjalkiRakennuspaikkaTotal = sumFields(
    'hiilikadenjalki',
    'rakennuspaikka',
    kadenjalkiFields
  );
</script>

<H2
  id="ilmastoselvitys"
  text={$_('energiatodistus.ilmastoselvitys.header')} />

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
  <h3 class="font-bold">
    {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.header')}
  </h3>
  <table class="w-full">
    <thead>
      <tr>
        <th />
        {#each jalanjalkiFields as field}
          <th class="px-2 text-sm">
            {$_(
              'energiatodistus.ilmastoselvitys.hiilijalanjalki.' + field
            )}
          </th>
        {/each}
        <th class="px-2 text-sm">
          {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.yhteensa')}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td class="pr-2 font-medium">
          {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus')}
        </td>
        {#each jalanjalkiFields as field}
          <td class="px-1">
            <Input
              {disabled}
              {schema}
              compact={true}
              center={true}
              bind:model={energiatodistus}
              path={['ilmastoselvitys', 'hiilijalanjalki', 'rakennus', field]} />
          </td>
        {/each}
        <td class="px-2 text-center">{jalanjalkiRakennusTotal}</td>
      </tr>
      <tr>
        <td class="pr-2 font-medium">
          {$_(
            'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka'
          )}
        </td>
        {#each jalanjalkiFields as field}
          <td class="px-1">
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
        <td class="px-2 text-center">{jalanjalkiRakennuspaikkaTotal}</td>
      </tr>
    </tbody>
  </table>
</div>

<div class="mb-8">
  <h3 class="font-bold">
    {$_('energiatodistus.ilmastoselvitys.hiilikadenjalki.header')}
  </h3>
  <table class="w-full">
    <thead>
      <tr>
        <th />
        {#each kadenjalkiFields as field}
          <th class="px-2 text-sm">
            {$_(
              'energiatodistus.ilmastoselvitys.hiilikadenjalki.' + field
            )}
          </th>
        {/each}
        <th class="px-2 text-sm">
          {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.yhteensa')}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td class="pr-2 font-medium">
          {$_('energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus')}
        </td>
        {#each kadenjalkiFields as field}
          <td class="px-1">
            <Input
              {disabled}
              {schema}
              compact={true}
              center={true}
              bind:model={energiatodistus}
              path={['ilmastoselvitys', 'hiilikadenjalki', 'rakennus', field]} />
          </td>
        {/each}
        <td class="px-2 text-center">{kadenjalkiRakennusTotal}</td>
      </tr>
      <tr>
        <td class="pr-2 font-medium">
          {$_(
            'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka'
          )}
        </td>
        {#each kadenjalkiFields as field}
          <td class="px-1">
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
        <td class="px-2 text-center">{kadenjalkiRakennuspaikkaTotal}</td>
      </tr>
    </tbody>
  </table>
</div>
