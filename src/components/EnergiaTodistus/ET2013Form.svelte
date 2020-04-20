<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Either from '@Utility/either-utils';
  import * as validation from '@Utility/validation';
  import * as et from './energiatodistus-utils';

  import H1 from '@Component/H/H1';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  export let submit;
  export let energiatodistus;
  const originalEnergiatodistus = R.clone(energiatodistus);
  const schema = et.schema2013;
  export let disabled = false;

  let isValidForm = false;
  $: isValidForm = et.isValidForm(schema, energiatodistus);
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
    <H1 text="Energiatodistus 2013 - not implemented" />
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
