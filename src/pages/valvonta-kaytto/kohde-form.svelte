<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Validation from '@Utility/validation';
  import * as Locales from '@Language/locale-utils';
  import * as Ilmoituspaikka from './ilmoituspaikka';

  import { _, locale } from '@Language/i18n';
  import { kohde as schema } from '@Pages/valvonta-kaytto/schema';

  import H2 from '@Component/H/H2.svelte';
  import Input from '@Component/Input/Input.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Datepicker from '@Component/Input/Datepicker';
  import Button from '@Component/Button/Button.svelte';
  import Confirm from '@Component/Confirm/Confirm.svelte';

  import { flashMessageStore } from '@/stores';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.kohde';

  export let kohde;
  export let ilmoituspaikat;

  export let save;
  export let revert;
  export let remove = Maybe.None();
  export let dirty;

  let form;

  const submit = _ => {
    if (Validation.isValidForm(schema)(kohde)) {
      save(kohde);
    } else {
      flashMessageStore.add(
        'valvonta-kaytto',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(form);
    }
  };
</script>
<form
    class="content"
    bind:this={form}
    on:submit|preventDefault={submit}
    on:input={_ => { dirty = true; }}
    on:change={_ => { dirty = true;}}>

  <div class="flex flex-col w-full py-8">
    <H2 text={i18n(`${i18nRoot}.rakennuksen-tiedot`)}/>
    <div class="py-4 w-full md:w-1/3">
      <Input
          id={'kohde.rakennustunnus'}
          name={'kohde.rakennustunnus'}
          label={i18n(`${i18nRoot}.rakennustunnus`)}
          bind:model={kohde}
          lens={R.lensProp('rakennustunnus')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          validators={schema.rakennustunnus}
          {i18n} />
    </div>
    <div class="py-4 w-full md:w-1/2">
      <Input
          id={'kohde.katuosoite'}
          name={'kohde.katuosoite'}
          label={i18n(`${i18nRoot}.katuosoite`)}
          required={true}
          bind:model={kohde}
          lens={R.lensProp('katuosoite')}
          parse={R.trim}
          validators={schema.katuosoite}
          {i18n}/>
    </div>
    <div class="py-4 w-full md:w-1/3">
      <Input
          id={'kohde.postinumero'}
          name={'kohde.postinumero'}
          label={i18n(`${i18nRoot}.postinumero`)}
          bind:model={kohde}
          lens={R.lensProp('postinumero')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          validators={schema.postinumero}
          {i18n}/>
    </div>
  </div>
  <div class="flex flex-col w-full py-8">
    <H2 text={i18n(`${i18nRoot}.ilmoituksen-tiedot`)}/>

    <div class="py-4 w-full md:w-1/3">
      <Datepicker
          label={i18n(`${i18nRoot}.havaintopaiva`)}
          bind:model={kohde}
          lens={R.lensProp('havaintopaiva')}
          format={Maybe.fold('', Formats.formatDateInstant)}
          parse={Parsers.optionalParser(Parsers.parseDate)}
          transform={EM.fromNull}
          {i18n}/>
    </div>
    <div class="py-4 w-full md:w-1/3">
      <Select
          id={'kohde.ilmoituspaikka-id'}
          label={i18n(`${i18nRoot}.ilmoituspaikka-id`)}
          required={false}
          disabled={false}
          allowNone={true}
          bind:model={kohde}
          parse={Maybe.fromNull}
          lens={R.lensProp('ilmoituspaikka-id')}
          format={Locales.labelForId($locale, ilmoituspaikat)}
          items={R.pluck('id', ilmoituspaikat)}/>
    </div>
    {#if Ilmoituspaikka.other(kohde)}
      <div class="py-4 w-full md:w-1/3">
        <Input
            id={'kohde.ilmoituspaikka-description'}
            name={'kohde.ilmoituspaikka-description'}
            label={i18n(`${i18nRoot}.ilmoituspaikka-description`)}
            bind:model={kohde}
            lens={R.lensProp('ilmoituspaikka-description')}
            parse={Parsers.optionalString}
            format={Maybe.orSome('')}
            validators={schema['ilmoituspaikka_description']}
            {i18n}/>
      </div>
    {/if}
    <div class="py-4 w-full md:w-1/3">
      <Input
          id={'kohde.ilmoitustunnus'}
          name={'kohde.ilmoitustunnus'}
          label={i18n(`${i18nRoot}.ilmoitustunnus`)}
          bind:model={kohde}
          lens={R.lensProp('ilmoitustunnus')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          {i18n}/>
    </div>
  </div>
  <div class="flex space-x-4 py-8">
    <Button
        disabled={!dirty}
        type={'submit'}
        text={i18n(`${i18nRoot}.save`)}/>
    <Button
        disabled={!dirty}
        on:click={revert}
        text={i18n(`${i18nRoot}.revert`)}
        style={'secondary'}/>
    {#each Maybe.toArray(remove) as deleteKohde}
      <Confirm
          let:confirm
          confirmButtonLabel={i18n('confirm.button.delete')}
          confirmMessage={i18n('confirm.you-want-to-delete')}>
        <Button
            on:click={() => {confirm(_ => deleteKohde(kohde.id));}}
            text={i18n(`${i18nRoot}.delete`)}
            style={'error'}/>
      </Confirm>
    {/each}
  </div>
</form>