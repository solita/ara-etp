<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Either from '@Utility/either-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as validation from '@Utility/validation';
  import * as Future from '@Utility/future-utils';
  import * as et from './energiatodistus-utils';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as api from './energiatodistus-api';

  import H1 from '@Component/H1/H1';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';
  import Select from '@Component/Select/Select';

  import { flashMessageStore } from '@/stores';

  export let title = '';
  export let submit;
  export let energiatodistus;
  const originalEnergiatodistus = R.clone(energiatodistus);
  const schema = et.schema2018;

  export let disabled = false;

  $: labelLocale = LocaleUtils.label($locale);

  let kielisyys = Either.Left('Not initialized');
  Future.fork(
    _ => {},
    result => kielisyys = Either.Right(result),
    api.kielisyys
  );

  let laatimisvaiheet = Either.Left('Not initialized');
  Future.fork(
    _ => {},
    result => laatimisvaiheet = Either.Right(result),
    api.laatimisvaiheet
  );

  let isValidForm = false;
  $: isValidForm = et.isValidForm(schema, energiatodistus);
  $: console.log('Form validation: ', isValidForm);
</script>

<form
  on:submit|preventDefault={_ => {
    if (isValidForm) {
      flashMessageStore.flush();
      submit(energiatodistus);
    } else {
      flashMessageStore.add('EnergiaTodistus', 'error', $_('energiatodistus.messages.validation-error'));
    }
  }}>
  <div class="w-full mt-3">
    <H1 text={title} />
    <div class="flex flex-col py-4 -mx-4">

      <div class="lg:w-1/2 w-full px-4 py-4">
        <Input
            id={'perustiedot.yritys.nimi'}
            name={'perustiedot.yritys.nimi'}
            label={$_('energiatodistus.perustiedot.yritys.nimi')}
            required={false}
            {disabled}
            bind:model={energiatodistus}
            lens={R.lensPath(['perustiedot', 'yritys', 'nimi'])}
            format={et.formatters.optionalText}
            parse={et.parsers.optionalText}
            validators={schema.perustiedot.nimi}
            i18n={$_} />
      </div>

      <div class="lg:w-1/2 w-full px-4 py-4">
        <Input
            id={'perustiedot.tilaaja'}
            name={'perustiedot.tilaaja'}
            label={$_('energiatodistus.perustiedot.tilaaja')}
            required={false}
            {disabled}
            bind:model={energiatodistus}
            lens={R.lensPath(['perustiedot', 'tilaaja'])}
            format={et.formatters.optionalText}
            parse={et.parsers.optionalText}
            validators={schema.perustiedot.nimi}
            i18n={$_} />
      </div>

      <div class="lg:w-1/2 w-full px-4 py-4">
        <Select
            id={'perustiedot.kieli'}
            name={'perustiedot.kieli'}
            label={$_('energiatodistus.perustiedot.kieli')}
            required={false}
            {disabled}
            bind:model={energiatodistus}
            lens={R.lensPath(['perustiedot', 'kieli'])}
            parse={Maybe.Some}
            format={et.selectFormat(labelLocale, kielisyys)}
            items={Either.foldRight([], R.pluck('id'), kielisyys)} />
      </div>

      <div class="lg:w-1/2 w-full px-4 py-4">
        <Select
            id={'perustiedot.laatimisvaihe'}
            name={'perustiedot.laatimisvaihe'}
            label={$_('energiatodistus.perustiedot.laatimisvaihe')}
            required={false}
            {disabled}
            bind:model={energiatodistus}
            lens={R.lensPath(['perustiedot', 'laatimisvaihe'])}
            parse={Maybe.Some}
            format={et.selectFormat(labelLocale, laatimisvaiheet)}
            items={Either.foldRight([], R.pluck('id'), laatimisvaiheet)} />
      </div>
    </div>

    <H1 text="Rakennuksen perustiedot" />
    <div class="flex lg:flex-row flex-col py-4 -mx-4">

      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input
          id={'nimi'}
          name={'nimi'}
          label={$_('energiatodistus.nimi')}
          required={false}
          {disabled}
          bind:model={energiatodistus}
          lens={R.lensPath(['perustiedot', 'nimi'])}
          format={et.formatters.optionalText}
          parse={et.parsers.optionalText}
          validators={schema.perustiedot.nimi}
          i18n={$_} />
      </div>
    </div>
  </div>
  <div class="flex -mx-4 pt-8">
    <div class="px-4">
      <Button type={'submit'} text={$_('tallenna')} />
    </div>
    <div class="px-4">
      <Button
        on:click={event => {
          event.preventDefault();
          energiatodistus = R.clone(originalEnergiatodistus);
        }}
        text={$_('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
