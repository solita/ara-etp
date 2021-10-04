<script>
  import * as R from 'ramda';
  import * as Schema from '@Pages/kayttaja/schema';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Validation from '@Utility/validation';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';

  import { flashMessageStore } from '@/stores';
  import { locale, _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import Checkbox from '@Component/Checkbox/Checkbox.svelte';
  import Select from '@Component/Select/Select.svelte';

  export let kayttaja;
  export let dirty;
  export let submit;
  export let cancel;
  export let whoami;
  export let roolit;

  const i18n = $_;
  const i18nRoot = 'kayttaja';

  const schema = Schema.Kayttaja;
  $: virtuSchema = Schema.virtuSchema(kayttaja);

  $: isValidForm = Validation.isValidForm(schema);

  $: isPaakayttaja = Kayttajat.isPaakayttaja(whoami);
  $: isOwnSettings = R.eqProps('id', kayttaja, whoami);
  $: disabled =
    Kayttajat.isSystem(kayttaja) || (!isPaakayttaja && !isOwnSettings);
  $: disabledAdmin = Kayttajat.isSystem(kayttaja) || !isPaakayttaja;

  $: formatRooli = Locales.labelForId($locale, roolit);

  const filterRoolit = R.filter(
    R.propSatisfies(
      R.complement(
        R.anyPass([Kayttajat.isLaatijaRole, Kayttajat.isSystemRole])
      ),
      'id'
    )
  );

  $: if (Kayttajat.isLaatija(kayttaja)) {
    throw 'This form should not be used for laatija.';
  }

  const emptyVirtuId = { organisaatio: '', localid: '' };

  let form;
  const saveKayttaja = _ => {
    if (isValidForm(kayttaja)) {
      flashMessageStore.flush();
      submit(kayttaja);
    } else {
      flashMessageStore.add(
        'kayttaja',
        'error',
        i18n('kayttaja.messages.validation-error')
      );
      Validation.blurForm(form);
    }
  };

  const setDirty = _ => {
    dirty = true;
  }
</script>

<style type="text/postcss">
  .lastlogin {
    @apply text-secondary mb-4;
  }
</style>

<form bind:this={form}
      on:submit|preventDefault={saveKayttaja}
      on:input={setDirty}
      on:change={setDirty}>
  <div class="w-full mt-3">
    <H1 text={kayttaja.etunimi + ' ' + kayttaja.sukunimi} />

    {#if !R.isNil(kayttaja.id)}
      <span class="lastlogin">
        {R.compose(
          Maybe.orSome(i18n('kayttaja.no-login')),
          R.map(R.concat(i18n('kayttaja.last-login') + ' ')),
          R.map(Formats.formatTimeInstant)
        )(kayttaja.login)}
      </span>
    {/if}

    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Checkbox
          bind:model={kayttaja}
          lens={R.lensProp('passivoitu')}
          label={$_(i18nRoot + '.passivoitu')}
          disabled={disabledAdmin || isOwnSettings} />
      </div>

      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Select
          id={'rooli'}
          label={$_(i18nRoot + '.rooli')}
          required={true}
          validation={true}
          allowNone={false}
          disabled={disabledAdmin || isOwnSettings}
          bind:model={kayttaja}
          lens={R.lensProp('rooli')}
          format={formatRooli}
          items={R.pluck('id', filterRoolit(roolit))} />
      </div>
    </div>

    <H2 text="Perustiedot" />

    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'etunimi'}
          name={'etunimi'}
          label={i18n('kayttaja.etunimi')}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('etunimi')}
          parse={R.trim}
          validators={schema.etunimi}
          {disabled}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sukunimi'}
          name={'sukunimi'}
          label={i18n('kayttaja.sukunimi')}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('sukunimi')}
          parse={R.trim}
          validators={schema.sukunimi}
          {disabled}
          {i18n} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4 items-end">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'email'}
          name={'email'}
          label={`${i18n('kayttaja.email')}`}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('email')}
          parse={R.trim}
          validators={schema.email}
          {disabled}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'puhelin'}
          name={'puhelin'}
          label={i18n('kayttaja.puhelin')}
          required={false}
          bind:model={kayttaja}
          lens={R.lensProp('puhelin')}
          parse={R.trim}
          validators={schema.puhelin}
          {disabled}
          {i18n} />
      </div>
    </div>

    <H2 text={i18n('kayttaja.virtu.header')} />
    <Checkbox
      bind:model={kayttaja}
      lens={R.compose(
        R.lensProp('virtu'),
        R.lens(Maybe.isSome, active =>
          active ? Maybe.Some(emptyVirtuId) : Maybe.None()
        )
      )}
      label={'Virtu-kirjautuminen käytössä'}
      disabled={disabledAdmin} />

    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      {#if Maybe.isSome(kayttaja.virtu)}
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'virtu.organisaatio'}
            name={'virtu.organisaatio'}
            label={i18n('kayttaja.virtu.organisaatio')}
            required={false}
            disabled={disabledAdmin}
            bind:model={kayttaja}
            lens={R.compose(
              R.lensProp('virtu'),
              R.lens(Maybe.orSome(emptyVirtuId), Maybe.Some),
              R.lensProp('organisaatio')
            )}
            parse={R.trim}
            validators={virtuSchema.organisaatio}
            {i18n} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'virtu.localid'}
            name={'virtu.localid'}
            label={i18n('kayttaja.virtu.localid')}
            required={false}
            disabled={disabledAdmin}
            bind:model={kayttaja}
            lens={R.compose(
              R.lensProp('virtu'),
              R.lens(Maybe.orSome(emptyVirtuId), Maybe.Some),
              R.lensProp('localid')
            )}
            parse={R.trim}
            validators={virtuSchema.localid}
            {i18n} />
        </div>
      {/if}
    </div>

    <H2 text={i18n('kayttaja.suomifi.header')} />
    <Checkbox
      bind:model={kayttaja}
      lens={R.compose(
        R.lensProp('henkilotunnus'),
        R.lens(Maybe.isSome, active => (active ? Maybe.Some('') : Maybe.None()))
      )}
      label={'Suomi-fi-kirjautuminen käytössä'}
      disabled={disabledAdmin} />

    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'henkilotunnus'}
          name={'henkilotunnus'}
          label={i18n('kayttaja.suomifi.henkilotunnus')}
          bind:model={kayttaja}
          lens={R.lensProp('henkilotunnus')}
          format={Maybe.orSome('')}
          parse={R.compose(Maybe.fromEmpty, R.trim)}
          validators={schema.henkilotunnus}
          disabled={disabledAdmin}
          {i18n} />
      </div>
    </div>
  </div>

  <div class="flex -mx-4 mt-10">
    <div class="px-4">
      <Button type={'submit'} text={i18n('tallenna')} disabled={disabled || !dirty} />
    </div>
    <div class="px-4">
      <Button
        on:click={cancel}
        disabled={disabled || !dirty}
        text={i18n(i18nRoot + '.cancel')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
