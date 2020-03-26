<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';

  import H1 from '@Component/H1/H1';
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Autocomplete from '@Component/Autocomplete/Autocomplete';
  import Select from '@Component/Select/Select';
  import * as LaatijaUtils from './laatija-utils';
  import {
    countryStore,
    toimintaAlueetStore,
    flashMessageStore
  } from '@/stores';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as country from '@Component/Geo/country-utils';
  import * as Validation from '@Utility/validation';
  import * as ToimintaAlueUtils from '@Component/Geo/toimintaalue-utils';

  const formParsers = LaatijaUtils.formParsers();
  const formSchema = LaatijaUtils.formSchema();

  export let laatija;
  export let submit;

  const originalLaatija = R.clone(laatija);

  const parseCountry = R.compose(
    R.map(R.prop('id')),
    R.chain(Maybe.toEither(R.applyTo('country-not-found'))),
    Either.leftMap(R.always(R.applyTo('connection-failure'))),
    fn => $countryStore.map(fn),
    country.findCountry
  );

  $: labelLocale = LocaleUtils.label($locale);

  $: formatCountry = R.compose(
    Either.orSome(R.__, ''),
    R.map(R.prop(labelLocale)),
    R.chain(Maybe.toEither('')),
    R.map(R.__, $countryStore),
    country.findCountryById
  );

  $: countryNames = Either.foldRight([], R.map(labelLocale), $countryStore);

  $: toimintaAlueNames = Either.foldRight(
    [],
    R.map(labelLocale),
    $toimintaAlueetStore
  );

  $: formatToimintaAlue = R.compose(
    labelLocale,
    ToimintaAlueUtils.findToimintaAlueById($toimintaAlueetStore)
  );

  $: parseToimintaAlue = R.compose(
    R.prop('id'),
    ToimintaAlueUtils.findToimintaAlue($toimintaAlueetStore)
  );
</script>

<form
  on:submit|preventDefault={_ => {
    const isValidForm = R.compose( R.all(Either.isRight), R.filter(Either.isEither), R.values, Validation.validateModelObject(formSchema) )(laatija);
    if (isValidForm) {
      flashMessageStore.flush();
      submit(laatija);
    } else {
      flashMessageStore.add('Laatija', 'error');
    }
  }}>
  <div class="w-full mt-3">
    <H1 text="Perustiedot" />
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'henkilotunnus'}
          name={'henkilotunnus'}
          label={$_('laatija.henkilotunnus')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('henkilotunnus')}
          parse={formParsers.henkilotunnus}
          validators={formSchema.henkilotunnus}
          disabled={true}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'etunimi'}
          name={'etunimi'}
          label={$_('laatija.etunimi')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('etunimi')}
          parse={formParsers.etunimi}
          validators={formSchema.etunimi}
          i18n={$_} />
      </div>

      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sukunimi'}
          name={'sukunimi'}
          label={$_('laatija.sukunimi')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('sukunimi')}
          parse={formParsers.sukunimi}
          validators={formSchema.sukunimi}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sahkoposti'}
          name={'sahkoposti'}
          label={`${$_('laatija.sahkoposti')}(${R.toLower($_('laatija.kayttajatunnus'))})`}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('email')}
          parse={formParsers.email}
          validators={formSchema.email}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'puhelinnumero'}
          name={'puhelinnumero'}
          label={$_('laatija.puhelinnumero')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('puhelin')}
          parse={formParsers.puhelin}
          validators={formSchema.puhelin}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:py-0 w-full px-4 py-4">
        <Input
          id={'katuosoite'}
          name={'katuosoite'}
          label={$_('laatija.katuosoite')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('jakeluosoite')}
          parse={formParsers.jakeluosoite}
          validators={formSchema.jakeluosoite}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postinumero'}
          name={'postinumero'}
          label={$_('laatija.postinumero')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('postinumero')}
          parse={formParsers.postinumero}
          validators={formSchema.postinumero}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postitoimipaikka'}
          name={'postitoimipaikka'}
          label={$_('laatija.postitoimipaikka')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('postitoimipaikka')}
          parse={formParsers.postitoimipaikka}
          validators={formSchema.postitoimipaikka}
          i18n={$_} />
      </div>

      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Autocomplete items={countryNames}>
          <Input
            id={'maa'}
            name={'maa'}
            label={$_('laatija.maa')}
            required={true}
            bind:model={laatija}
            lens={R.lensProp('maa')}
            format={formatCountry}
            parse={parseCountry}
            i18n={$_} />
        </Autocomplete>
      </div>
    </div>
  </div>
  <div class="mt-8">
    <H1 text={$_('laatija.laatijatiedot')} />
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'patevyydenvoimassaolo'}
          name={'patevyydenvoimassaolo'}
          label={$_('laatija.patevyydenvoimassaolo')}
          disabled={true}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'patevyystaso'}
          name={'patevyystaso'}
          label={$_('laatija.patevyystaso')}
          disabled={true}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <!--Select
          format={formatToimintaAlue}
          parse={parseToimintaAlue}
          bind:model={laatija}
          lens={R.lensProp('toimintaalue')}
          items={toimintaAlueNames} /-->
      </div>
    </div>
  </div>

  <div class="mt-8">
    <H1 text={$_('laatija.julkisettiedot')} />
    <div class="flex flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 mb-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinen-puhelin')}
          label={$_('laatija.puhelinnumero')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinen-email')}
          label={$_('laatija.sahkoposti')} />
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
          laatija = R.clone(originalLaatija);
        }}
        text={$_('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
