<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Parsers from '@Utility/parsers';
  import * as Validation from '@Utility/validation';
  import * as Osapuolet from './osapuolet';

  import { _, locale } from '@Language/i18n';
  import { henkilo as schema } from '@Pages/valvonta-kaytto/schema';
  import { flashMessageStore } from '@/stores';

  import Input from '@Component/Input/Input.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Confirm from '@Component/Confirm/Confirm';
  import ContactDetails from './contact-details-form.svelte';

  export let osapuoli;
  export let roolit;
  export let toimitustavat;
  export let countries;
  export let save;
  export let revert;
  export let remove = Maybe.None();
  export let dirty = false;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuoli';
  let form;

  const setDirty = _ => {
    dirty = true;
  };

  const submit = _ => {
    if (Validation.isValidForm(schema)(osapuoli)) {
      save(osapuoli);
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
  on:input={setDirty}
  on:change={setDirty}
  on:text-change={setDirty}>
  <div class="flex flex-col w-full py-8">
    <div
      class="py-4 flex flex-col md:flex-row md:space-x-4 space-y-4 md:space-y-0 w-full md:w-2/3">
      <div class="w-full">
        <Input
          id={'henkilo.etunimi'}
          name={'henkilo.etunimi'}
          label={i18n(`${i18nRoot}.etunimi`)}
          required={true}
          bind:model={osapuoli}
          lens={R.lensProp('etunimi')}
          parse={R.trim}
          validators={schema.etunimi}
          {i18n} />
      </div>
      <div class="w-full">
        <Input
          id={'henkilo.sukunimi'}
          name={'henkilo.sukunimi'}
          label={i18n(`${i18nRoot}.sukunimi`)}
          required={true}
          bind:model={osapuoli}
          lens={R.lensProp('sukunimi')}
          parse={R.trim}
          validators={schema.sukunimi}
          {i18n} />
      </div>
    </div>

    <div class="py-4 w-full md:w-1/3 md:pr-2">
      <Input
        id={'henkilo.henkilotunnus'}
        name={'henkilo.henkilotunnus'}
        label={i18n(`${i18nRoot}.henkilotunnus`)}
        bind:model={osapuoli}
        lens={R.lensProp('henkilotunnus')}
        parse={Parsers.optionalString}
        format={Maybe.orSome('')}
        validators={schema.henkilotunnus}
        {i18n} />
    </div>

    <div class="py-4 w-full md:w-1/3 md:pr-2">
      <Select
        id={'henkilo.rooli-id'}
        label={i18n(`${i18nRoot}.rooli-id`)}
        required={false}
        disabled={false}
        allowNone={true}
        bind:model={osapuoli}
        parse={Maybe.fromNull}
        lens={R.lensProp('rooli-id')}
        format={Locales.labelForId($locale, roolit)}
        items={R.pluck('id', roolit)} />
    </div>
    {#if Osapuolet.otherRooli(osapuoli)}
      <div class="py-4 w-full md:w-1/3 md:pr-2">
        <Input
          id={'henkilo.rooli-description'}
          name={'henkilo.rooli-description'}
          label={i18n(`${i18nRoot}.rooli-description`)}
          bind:model={osapuoli}
          lens={R.lensProp('rooli-description')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          validators={schema['rooli-description']}
          {i18n} />
      </div>
    {/if}

    <ContactDetails {osapuoli} {schema}
                    {toimitustavat}
                    {countries}/>
  </div>
  <div class="flex flex-col">
    {#if Osapuolet.toimitustapa.suomifi(osapuoli) && Maybe.None(osapuoli.henkilotunnus)}
      <div class="flex space-x-2">
        <span class="font-icon text-warning">info</span>
        <span>
          {i18n(`${i18nRoot}.warning-toimitustapa-suomifi-henkilo`)}
        </span>
      </div>
    {/if}
    {#if Osapuolet.toimitustapa.email(osapuoli) && Maybe.None(osapuoli.email)}
      <div class="flex space-x-2">
        <span class="font-icon text-warning">info</span>
        <span>
          {i18n(`${i18nRoot}.warning-toimitustapa-email`)}
        </span>
      </div>
    {/if}
  </div>
  <div class="flex space-x-4 py-8">
    <Button disabled={!dirty} type={'submit'} text={i18n(`${i18nRoot}.save`)} />
    <Button
        disabled={!dirty}
        on:click={revert}
        text={i18n(`${i18nRoot}.revert`)}
        style={'secondary'}/>
    {#each Maybe.toArray(remove) as deleteHenkilo}
      <Confirm
        let:confirm
        confirmButtonLabel={i18n('confirm.button.delete')}
        confirmMessage={i18n('confirm.you-want-to-delete')}>
        <Button
          on:click={() => {
            confirm(_ => deleteHenkilo(osapuoli.id));
          }}
          text={i18n(`${i18nRoot}.delete`)}
          style={'error'} />
      </Confirm>
    {/each}
  </div>
</form>
