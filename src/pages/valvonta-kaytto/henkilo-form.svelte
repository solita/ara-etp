<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Parsers from '@Utility/parsers';

  import Input from '@Component/Input/Input.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Confirm from '@Component/Confirm/Confirm';
  import { _, locale } from '@Language/i18n';

  export let henkilo;
  export let roolit;
  export let toimitustavat;
  export let countries;
  export let submitHenkilo;
  export let deleteHenkilo;
  export let i18n;
  export let i18nRoot;
  export let dirty;

  let form;

  const submit = event => {
    submitHenkilo(henkilo, event);
  };

  const setDirty = _ => {
    dirty = true;
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
          bind:model={henkilo}
          lens={R.lensProp('etunimi')}
          parse={R.trim}
          {i18n} />
      </div>
      <div class="w-full">
        <Input
          id={'henkilo.sukunimi'}
          name={'henkilo.sukunimi'}
          label={i18n(`${i18nRoot}.sukunimi`)}
          required={true}
          bind:model={henkilo}
          lens={R.lensProp('sukunimi')}
          parse={R.trim}
          {i18n} />
      </div>
    </div>

    <div class="py-4 w-full md:w-1/3 md:pr-2">
      <Input
        id={'henkilo.henkilotunnus'}
        name={'henkilo.henkilotunnus'}
        label={i18n(`${i18nRoot}.henkilotunnus`)}
        bind:model={henkilo}
        lens={R.lensProp('henkilotunnus')}
        parse={Parsers.optionalString}
        format={Maybe.orSome('')}
        {i18n} />
    </div>

    <div class="py-4 w-full md:w-1/3 md:pr-2">
      <Select
        id={'henkilo.rooli-id'}
        label={i18n(`${i18nRoot}.rooli-id`)}
        required={false}
        disabled={false}
        allowNone={true}
        bind:model={henkilo}
        parse={Maybe.fromNull}
        lens={R.lensProp('rooli-id')}
        format={Locales.labelForId($locale, roolit)}
        items={R.pluck('id', roolit)} />
    </div>
    {#each Maybe.toArray(henkilo['rooli-id']) as rooliId}
      {#if rooliId === 2}
        <div class="py-4 w-full md:w-1/3 md:pr-2">
          <Input
            id={'henkilo.rooli-description'}
            name={'henkilo.rooli-description'}
            label={i18n(`${i18nRoot}.rooli-description`)}
            bind:model={henkilo}
            lens={R.lensProp('rooli-description')}
            parse={R.trim}
            {i18n} />
        </div>
      {/if}
    {/each}

    <div
      class="py-4 flex flex-col md:flex-row md:space-x-4 space-y-4 md:space-y-0 w-full md:w-2/3">
      <div class="w-full">
        <Input
          id={'henkilo.email'}
          name={'henkilo.email'}
          label={i18n(`${i18nRoot}.email`)}
          bind:model={henkilo}
          lens={R.lensProp('email')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          {i18n} />
      </div>
      <div class="w-full">
        <Input
          id={'henkilo.puhelin'}
          name={'henkilo.puhelin'}
          label={i18n(`${i18nRoot}.puhelin`)}
          bind:model={henkilo}
          lens={R.lensProp('puhelin')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          {i18n} />
      </div>
    </div>

    <div class="py-4 w-full md:w-1/3 md:pr-2">
      <Input
        id={'henkilo.vastaanottajan-tarkenne'}
        name={'henkilo.vastaanottajan-tarkenne'}
        label={i18n(`${i18nRoot}.vastaanottajan-tarkenne`)}
        bind:model={henkilo}
        lens={R.lensProp('vastaanottajan-tarkenne')}
        parse={Parsers.optionalString}
        format={Maybe.orSome('')}
        {i18n} />
    </div>
    <div class="py-4 w-full">
      <Input
        id={'henkilo.jakeluosoite'}
        name={'henkilo.jakeluosoite'}
        label={i18n(`${i18nRoot}.jakeluosoite`)}
        bind:model={henkilo}
        lens={R.lensProp('jakeluosoite')}
        parse={Parsers.optionalString}
        format={Maybe.orSome('')}
        {i18n} />
    </div>
    <div
      class="py-4 flex flex-col md:flex-row md:space-x-4 space-y-4 md:space-y-0 w-full">
      <div class="w-full">
        <Input
          id={'henkilo.postinumero'}
          name={'henkilo.postinumero'}
          label={i18n(`${i18nRoot}.postinumero`)}
          bind:model={henkilo}
          lens={R.lensProp('postinumero')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          {i18n} />
      </div>
      <div class="w-full">
        <Input
          id={'henkilo.postitoimipaikka'}
          name={'henkilo.postitoimipaikka'}
          label={i18n(`${i18nRoot}.postitoimipaikka`)}
          bind:model={henkilo}
          lens={R.lensProp('postitoimipaikka')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          {i18n} />
      </div>
      <div class="w-full">
        <Select
          id={'henkilo.maa'}
          label={i18n(`${i18nRoot}.maa`)}
          required={false}
          disabled={false}
          allowNone={true}
          bind:model={henkilo}
          parse={Maybe.fromNull}
          lens={R.lensProp('maa')}
          format={Locales.labelForId($locale, countries)}
          items={R.pluck('id', countries)} />
      </div>
    </div>
    <div class="py-4 w-full md:w-1/3 md:pr-2">
      <Select
        id={'henkilo.toimitustapa-id'}
        label={i18n(`${i18nRoot}.toimitustapa-id`)}
        required={false}
        disabled={false}
        allowNone={true}
        bind:model={henkilo}
        parse={Maybe.fromNull}
        lens={R.lensProp('toimitustapa-id')}
        format={Locales.labelForId($locale, toimitustavat)}
        items={R.pluck('id', toimitustavat)} />
    </div>
  </div>
  {#each Maybe.toArray(henkilo['toimitustapa-id']) as toimitustapa}
    <div class="flex flex-col">
      {#if toimitustapa === 0 && !henkilo.henkilotunnus}
        <div class="flex space-x-2">
          <span class="font-icon text-warning">info</span>
          <span>
            {i18n(`${i18nRoot}.warning-toimitustapa-suomifi-henkilo`)}
          </span>
        </div>
      {/if}
      {#if toimitustapa === 1 && !henkilo.email}
        <div class="flex space-x-2">
          <span class="font-icon text-warning">info</span>
          <span>
            {i18n(`${i18nRoot}.warning-toimitustapa-email`)}
          </span>
        </div>
      {/if}
    </div>
  {/each}
  <div class="flex space-x-4 py-8">
    <Button disabled={!dirty} type={'submit'} text={i18n(`${i18nRoot}.save`)} />
    {#if deleteHenkilo}
      <Confirm
        let:confirm
        confirmButtonLabel={i18n('confirm.button.delete')}
        confirmMessage={i18n('confirm.you-want-to-delete')}>
        <Button
          on:click={() => {
            confirm(deleteHenkilo);
          }}
          text={i18n(`${i18nRoot}.delete`)}
          style={'error'} />
      </Confirm>
    {/if}
  </div>
</form>
