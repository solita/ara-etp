<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Parsers from '@Utility/parsers';
  import * as Osapuolet from './osapuolet';

  import Input from '@Component/Input/Input.svelte';
  import Select from '@Component/Select/Select.svelte';
  import H2 from '@Component/H/H2.svelte';

  import { _, locale } from '@Language/i18n';
  import Autocomplete from '../../components/Autocomplete/Autocomplete.svelte';
  import * as Country from '@Utility/country';

  export let osapuoli;
  export let toimitustavat;
  export let countries;
  export let schema;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuoli';

  $: parseCountry = R.compose(
    R.map(R.prop('id')),
    Country.findCountry(R.__, countries)
  );
</script>

<div class="mt-4">
  <H2 text={i18n(i18nRoot + '.contact-details-title')} />
</div>
<div
  class="py-4 flex flex-col md:flex-row md:space-x-4 space-y-4 md:space-y-0 w-full md:w-2/3">
  <div class="w-full">
    <Input
      id={'osapuoli.email'}
      name={'osapuoli.email'}
      label={i18n(`${i18nRoot}.email`)}
      bind:model={osapuoli}
      lens={R.lensProp('email')}
      parse={Parsers.optionalString}
      format={Maybe.orSome('')}
      validators={schema.email}
      {i18n} />
  </div>
  <div class="w-full">
    <Input
      id={'osapuoli.puhelin'}
      name={'osapuoli.puhelin'}
      label={i18n(`${i18nRoot}.puhelin`)}
      bind:model={osapuoli}
      lens={R.lensProp('puhelin')}
      parse={Parsers.optionalString}
      format={Maybe.orSome('')}
      validators={schema.puhelin}
      {i18n} />
  </div>
</div>

<div class="py-4 w-full md:w-1/3 md:pr-2">
  <Input
    id={'osapuoli.vastaanottajan-tarkenne'}
    name={'osapuoli.vastaanottajan-tarkenne'}
    label={i18n(`${i18nRoot}.vastaanottajan-tarkenne`)}
    bind:model={osapuoli}
    lens={R.lensProp('vastaanottajan-tarkenne')}
    parse={Parsers.optionalString}
    format={Maybe.orSome('')}
    validators={schema['vastaanottajan-tarkenne']}
    {i18n} />
</div>
<div class="py-4 w-full">
  <Input
    id={'osapuoli.jakeluosoite'}
    name={'osapuoli.jakeluosoite'}
    label={i18n(`${i18nRoot}.jakeluosoite`)}
    bind:model={osapuoli}
    lens={R.lensProp('jakeluosoite')}
    parse={Parsers.optionalString}
    format={Maybe.orSome('')}
    validators={schema.jakeluosoite}
    {i18n} />
</div>
<div
  class="py-4 flex flex-col md:flex-row md:space-x-4 space-y-4 md:space-y-0 w-full">
  <div class="w-full">
    <Input
      id={'osapuoli.postinumero'}
      name={'osapuoli.postinumero'}
      label={i18n(`${i18nRoot}.postinumero`)}
      bind:model={osapuoli}
      lens={R.lensProp('postinumero')}
      parse={Parsers.optionalString}
      format={Maybe.orSome('')}
      validators={schema.postinumero}
      {i18n} />
  </div>
  <div class="w-full">
    <Input
      id={'osapuoli.postitoimipaikka'}
      name={'osapuoli.postitoimipaikka'}
      label={i18n(`${i18nRoot}.postitoimipaikka`)}
      bind:model={osapuoli}
      lens={R.lensProp('postitoimipaikka')}
      parse={Parsers.optionalString}
      format={Maybe.orSome('')}
      validators={schema.postitoimipaikka}
      {i18n} />
  </div>
  <div class="w-full">
    <Autocomplete items={R.map(Locales.label($locale), countries)}>
      <Input
          id={'osapuoli.maa'}
          name={'osapuoli.maa'}
          label={i18n(`${i18nRoot}.maa`)}
          required={false}
          disabled={false}
          bind:model={osapuoli}
          lens={R.lensProp('maa')}
          format={Maybe.fold('', Locales.labelForId($locale, countries))}
          parse={parseCountry}
          search={true}
          {i18n}/>
    </Autocomplete>
  </div>
</div>
<div class="py-4 w-full md:w-1/3 md:pr-2">
  <Select
    id={'osapuoli.toimitustapa-id'}
    label={i18n(`${i18nRoot}.toimitustapa-id`)}
    required={false}
    disabled={false}
    allowNone={true}
    bind:model={osapuoli}
    parse={Maybe.fromNull}
    lens={R.lensProp('toimitustapa-id')}
    format={Locales.labelForId($locale, toimitustavat)}
    items={R.pluck('id', toimitustavat)} />
</div>
{#if Osapuolet.toimitustapa.other(osapuoli)}
  <div class="py-4 w-full md:w-1/3 md:pr-2">
    <Input
      id={'osapuoli.toimitustapa-description'}
      name={'osapuoli.toimitustapa-description'}
      label={i18n(`${i18nRoot}.toimitustapa-description`)}
      bind:model={osapuoli}
      lens={R.lensProp('toimitustapa-description')}
      parse={Parsers.optionalString}
      format={Maybe.orSome('')}
      validators={schema['toimitustapa-description']}
      {i18n} />
  </div>
{/if}
