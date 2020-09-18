<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import * as KayttajaSchema from '@Component/Kayttaja/schema';
  import { flashMessageStore, currentUserStore } from '@/stores';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as validation from '@Utility/validation';
  import * as formats from '@Utility/formats';
  import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';

  export let kayttaja;
  export let submit;

  const formSchema = KayttajaSchema[Kayttajat.roleKey(kayttaja.rooli)];
  const formParsers = KayttajaSchema.formParsers();

  const isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    R.values,
    validation.validateModelObject(formSchema)
  );
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
      flashMessageStore.add('Kayttaja', 'error');
    }
  }}>
  <div class="w-full mt-3">
    <H1 text="Perustiedot" />
    <span class="lastlogin">
      {R.compose( Maybe.orSome($_('kayttaja.no-login')), Maybe.map(R.concat($_('kayttaja.last-login') + ' ')), Maybe.map(formats.formatTimeInstant) )(kayttaja.login)}
    </span>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'etunimi'}
          name={'etunimi'}
          label={$_('kayttaja.etunimi')}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('etunimi')}
          parse={formParsers.etunimi}
          validators={formSchema.etunimi}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sukunimi'}
          name={'sukunimi'}
          label={$_('kayttaja.sukunimi')}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('sukunimi')}
          parse={formParsers.sukunimi}
          validators={formSchema.sukunimi}
          i18n={$_} />
      </div>
      {#if KayttajaUtils.isPatevyydentoteaja(kayttaja)}
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'henkilotunnus'}
            name={'henkilotunnus'}
            label={$_('laatija.henkilotunnus')}
            required={true}
            bind:model={kayttaja}
            lens={R.lensProp('henkilotunnus')}
            format={Maybe.orSome('')}
            parse={formParsers.henkilotunnus}
            validators={formSchema.henkilotunnus}
            disabled={true}
            i18n={$_} />
        </div>
       {/if}
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sahkoposti'}
          name={'sahkoposti'}
          label={`${$_('kayttaja.sahkoposti')}(${R.toLower($_('kayttaja.kayttajatunnus'))})`}
          required={true}
          disabled={true}
          bind:model={kayttaja}
          lens={R.lensProp('email')}
          parse={formParsers.email}
          validators={formSchema.email}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'puhelinnumero'}
          name={'puhelinnumero'}
          label={$_('kayttaja.puhelinnumero')}
          required={true}
          bind:model={kayttaja}
          lens={R.lensProp('puhelin')}
          parse={formParsers.puhelin}
          validators={formSchema.puhelin}
          i18n={$_} />
      </div>
    </div>
    {#if KayttajaUtils.isPaakayttaja(kayttaja)}
      <H1 text={$_('kayttaja.virtu')} />
      <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'virtuorganisaatio'}
            name={'virtuorganisaatio'}
            label={$_('kayttaja.virtuorganisaatio')}
            required={true}
            disabled={false}
            bind:model={kayttaja}
            format={Maybe.orSome('')}
            lens={R.lensPath(['virtu', 'organisaatio'])}
            parse={formParsers.virtu.organisaatio}
            validators={formSchema.virtu.organisaatio}
            i18n={$_} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'virtulocalid'}
            name={'virtulocalid'}
            label={$_('kayttaja.virtulocalid')}
            required={true}
            disabled={false}
            bind:model={kayttaja}
            format={Maybe.orSome('')}
            lens={R.lensPath(['virtu', 'localid'])}
            parse={formParsers.virtu.localid}
            validators={formSchema.virtu.localid}
            i18n={$_} />
        </div>
      </div>
    {/if}
  </div>

  <div class="flex -mx-4 mt-20">
    <div class="px-4">
      <Button type={'submit'} text={$_('tallenna')} />
    </div>
    <div class="px-4">
      <Button
        on:click={event => {
          event.preventDefault();
          window.location.reload();
        }}
        text={$_('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
