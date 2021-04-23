<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import * as KayttajaSchema from '@Component/Kayttaja/schema';
  import { flashMessageStore } from '@/stores';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as deep from '@Utility/deep-objects';
  import * as validation from '@Utility/validation';
  import * as formats from '@Utility/formats';

  export let kayttaja;
  export let submit;
  export let whoami;

  const i18n = $_;

  const formSchema = KayttajaSchema[Kayttajat.roleKey(kayttaja.rooli)];
  const formParsers = KayttajaSchema.formParsers();

  const isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    deep.values(Either.isEither),
    validation.validateModelObject(formSchema)
  );

  $: isPaakayttaja = Kayttajat.isPaakayttaja(whoami);
  $: isOwnSettings = R.eqProps('id', kayttaja, whoami);
  $: disabled = !R.or(isPaakayttaja, isOwnSettings);
</script>

<style type="text/postcss">
  .lastlogin {
    @apply text-secondary mb-4;
  }
</style>

<form
  on:submit|preventDefault={_ => {
    if (isValidForm(kayttaja)) {
      flashMessageStore.flush();
      submit(kayttaja);
    } else {
      flashMessageStore.add(
        'Kayttaja',
        'error',
        i18n('kayttaja.messages.validation-error')
      );
    }
  }}>
  <div class="w-full mt-3">
    <H1 text="Perustiedot" />
    <span class="lastlogin">
      {R.compose(
        Maybe.orSome(i18n('kayttaja.no-login')),
        Maybe.map(R.concat(i18n('kayttaja.last-login') + ' ')),
        Maybe.map(formats.formatTimeInstant)
      )(kayttaja.login)}
    </span>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'etunimi'}
          name={'etunimi'}
          label={i18n('kayttaja.etunimi')}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('etunimi')}
          parse={formParsers.etunimi}
          validators={formSchema.etunimi}
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
          parse={formParsers.sukunimi}
          validators={formSchema.sukunimi}
          {disabled}
          {i18n} />
      </div>
      {#if Kayttajat.isPatevyydentoteaja(kayttaja)}
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'henkilotunnus'}
            name={'henkilotunnus'}
            label={i18n('laatija.henkilotunnus')}
            required={true}
            bind:model={kayttaja}
            lens={R.lensProp('henkilotunnus')}
            format={Maybe.orSome('')}
            parse={formParsers.henkilotunnus}
            validators={formSchema.henkilotunnus}
            disabled={true}
            {i18n} />
        </div>
      {/if}
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4 items-end">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sahkoposti'}
          name={'sahkoposti'}
          label={`${i18n('kayttaja.sahkoposti')}`}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('email')}
          parse={formParsers.email}
          validators={formSchema.email}
          {disabled}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'puhelinnumero'}
          name={'puhelinnumero'}
          label={i18n('kayttaja.puhelinnumero')}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('puhelin')}
          parse={formParsers.puhelin}
          validators={formSchema.puhelin}
          {disabled}
          {i18n} />
      </div>
    </div>
    {#if Kayttajat.isPaakayttaja(kayttaja) && isPaakayttaja}
      <H1 text={i18n('kayttaja.virtu.header')} />
      <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'virtu.organisaatio'}
            name={'virtu.organisaatio'}
            label={i18n('kayttaja.virtu.organisaatio')}
            required={true}
            disabled={false}
            bind:model={kayttaja}
            lens={R.lensPath(['virtu', 'organisaatio'])}
            parse={formParsers.virtu.organisaatio}
            validators={formSchema.virtu.organisaatio}
            {i18n} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'virtu.localid'}
            name={'virtu.localid'}
            label={i18n('kayttaja.virtu.localid')}
            required={true}
            disabled={false}
            bind:model={kayttaja}
            lens={R.lensPath(['virtu', 'localid'])}
            parse={formParsers.virtu.localid}
            validators={formSchema.virtu.localid}
            {i18n} />
        </div>
      </div>
    {/if}
  </div>

  <div class="flex -mx-4 mt-20">
    <div class="px-4">
      <Button type={'submit'} text={i18n('tallenna')} {disabled} />
    </div>
    <div class="px-4">
      <Button
        on:click={event => {
          event.preventDefault();
          window.location.reload();
        }}
        text={i18n('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
