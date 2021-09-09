<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Parsers from '@Utility/parsers';
  import * as Osapuolet from './osapuolet';

  import Input from '@Component/Input/Input.svelte';
  import Select from '@Component/Select/Select.svelte';
  import { _, locale } from '@Language/i18n';

  export let osapuoli;
  export let toimitustavat;
  export let countries;
  export let schema;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuoli';
</script>

<div class="py-4 flex flex-col md:flex-row md:space-x-4 space-y-4 md:space-y-0 w-full md:w-2/3">
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
        {i18n} />
  </div>
  <div class="w-full">
    <Select
        id={'osapuoli.maa'}
        label={i18n(`${i18nRoot}.maa`)}
        required={false}
        disabled={false}
        allowNone={true}
        bind:model={osapuoli}
        parse={Maybe.fromNull}
        lens={R.lensProp('maa')}
        format={Locales.labelForId($locale, countries)}
        items={R.pluck('id', countries)} />
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