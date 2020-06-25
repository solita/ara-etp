<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as validation from '@Utility/validation';
  import * as YritysUtils from './yritys-utils';
  import * as country from '@Component/Geo/country-utils';

  import Autocomplete from '../Autocomplete/Autocomplete';
  import Select from '@Component/Select/Select';
  import H1 from '@Component/H/H1';
  import HR from '@Component/HR/HR';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';

  import * as laskutusApi from '@Component/Laskutus/laskutus-api';

  import { countryStore, flashMessageStore } from '@/stores';

  const update = fn => (yritys = fn(yritys));

  export let submit;
  export let existing = false;

  export let yritys;

  export let disabled = false;

  export let luokittelut = Maybe.None();

  $: Future.fork(
    flashMessageStore.add('Yritys', 'error'),
    ([laskutuskielet, verkkolaskuoperaattorit]) =>
      luokittelut = Maybe.Some({laskutuskielet: laskutuskielet,
                               verkkolaskuoperaattorit: verkkolaskuoperaattorit}),
    Future.parallel(2,[laskutusApi.laskutuskielet, laskutusApi.verkkolaskuoperaattorit])
  );

  const formParsers = YritysUtils.formParsers();
  const formSchema = YritysUtils.formSchema();

  $: labelLocale = LocaleUtils.label($locale);

  const parseCountry = R.compose(
    R.map(R.prop('id')),
    R.chain(Maybe.toEither(R.applyTo('country-not-found'))),
    Either.leftMap(R.always(R.applyTo('connection-failure'))),
    fn => $countryStore.map(fn),
    country.findCountry
  );

  $: formatCountry = R.compose(
    Either.orSome(''),
    R.map(LocaleUtils.label($locale)),
    R.chain(Maybe.toEither('')),
    R.map(R.__, $countryStore),
    country.findCountryById
  );

  $: countryNames = Either.foldRight(
    [],
    R.map(LocaleUtils.label($locale)),
    $countryStore
  );

  $: laskutuskieletIds = R.map(R.compose(R.pluck('id'), R.prop('laskutuskielet')), luokittelut);

  $: formatLaskutuskieli = id => R.compose(
    Maybe.orSome(''),
    R.map(labelLocale),
    R.chain(Maybe.findById(id)),
    R.map(R.prop('laskutuskielet'))
  )(luokittelut);

  $: parseLaskutuskieli = R.identity;

  $: verkkolaskuoperaattoritIds = R.map(R.compose(R.pluck('id'), R.prop('verkkolaskuoperaattorit')), luokittelut);

  $: formatVerkkolaskuoperaattori = id => R.compose(
    Maybe.orSome(''),
    R.map(vlo => vlo.nimi + ' - ' + vlo.valittajatunnus),
    R.chain(Maybe.findById(id)),
    R.map(R.prop('verkkolaskuoperaattorit'))
  )(luokittelut);

  $: parseVerkkolaskuoperaattori = Maybe.fromNull;

  const isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    R.values,
    validation.validateModelObject(formSchema)
  );

  const cancel = event => {
    event.preventDefault();
    window.location.reload();
  };
</script>

<form
  on:submit|preventDefault={_ => {
    if (isValidForm(yritys)) {
      flashMessageStore.flush();
      submit(yritys);
    } else {
      flashMessageStore.add('Yritys', 'error', $_('yritys.messages.validation-error'));
    }
  }}>
  <div class="w-full mt-3">
    <H1 text="Perustiedot" />
    <div class="flex lg:flex-row flex-col lg:py-4 -mx-4">
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input
          id={'ytunnus'}
          name={'ytunnus'}
          label={$_('yritys.y-tunnus')}
          required={true}
          bind:model={yritys}
          lens={R.lensProp('ytunnus')}
          parse={formParsers.ytunnus}
          validators={formSchema.ytunnus}
          i18n={$_}
          disabled={existing} />
      </div>
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input
          id={'nimi'}
          name={'nimi'}
          label={$_('yritys.nimi')}
          required={true}
          bind:model={yritys}
          lens={R.lensProp('nimi')}
          parse={formParsers.nimi}
          validators={formSchema.nimi}
          i18n={$_} />
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H1 text={$_('yritys.laskutusosoite')} />
    <div class="flex flex-col">
      <div class="py-4">
        <Input
          {disabled}
          id={'vastaanottajan-tarkenne'}
          name={'vastaanottajan-tarkenne'}
          label={$_('yritys.vastaanottajan-tarkenne')}
          required={false}
          bind:model={yritys}
          lens={R.lensProp('vastaanottajan-tarkenne')}
          format={Maybe.orSome('')}
          parse={formParsers['vastaanottajan-tarkenne']}
          validators={formSchema['vastaanottajan-tarkenne']}
          i18n={$_} />
      </div>
    </div>
    <div class="flex flex-col">
      <div class="py-4">
        <Input
          {disabled}
          id={'jakeluosoite'}
          name={'jakeluosoite'}
          label={$_('yritys.jakeluosoite')}
          required={true}
          bind:model={yritys}
          lens={R.lensProp('jakeluosoite')}
          parse={formParsers.jakeluosoite}
          validators={formSchema.jakeluosoite}
          i18n={$_} />
      </div>
      <div class="flex lg:flex-row flex-col lg:py-4 -mx-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            {disabled}
            id={'postinumero'}
            name={'postinumero'}
            label={$_('yritys.postinumero')}
            required={true}
            bind:model={yritys}
            lens={R.lensProp('postinumero')}
            parse={formParsers.postinumero}
            validators={formSchema.postinumero}
            i18n={$_} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            {disabled}
            id={'postitoimipaikka'}
            name={'postitoimipaikka'}
            label={$_('yritys.postitoimipaikka')}
            required={true}
            bind:model={yritys}
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
              label={$_('yritys.maa')}
              required={true}
              bind:model={yritys}
              lens={R.lensProp('maa')}
              format={formatCountry}
              parse={parseCountry}
              search={true}
              i18n={$_} />
          </Autocomplete>
        </div>
      </div>
      <div class="flex lg:flex-row flex-col py-4 -mx-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Select
            label={$_('yritys.laskutuskieli')}
            required={true}
            format={formatLaskutuskieli}
            parse={parseLaskutuskieli}
            bind:model={yritys}
            lens={R.lensProp('laskutuskieli')}
            allowNone={false}
            items={laskutuskieletIds.orSome([])} />
        </div>
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H1 text={$_('yritys.verkkolaskuosoite')} />
    <div class="lg:w-1/4 w-full">
      <Input
        {disabled}
        id={'verkkolaskuosoite'}
        name={'verkkolaskuosoite'}
        label={$_('yritys.ovt-tunnus')}
        bind:model={yritys}
        lens={R.lensProp('verkkolaskuosoite')}
        format={Maybe.orSome('')}
        parse={formParsers.verkkolaskuosoite}
        i18n={$_} />
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Select
          label={$_('yritys.verkkolaskuoperaattori')}
          required={false}
          format={formatVerkkolaskuoperaattori}
          parse={parseVerkkolaskuoperaattori}
          bind:model={yritys}
          lens={R.lensProp('verkkolaskuoperaattori')}
          allowNone={true}
          items={verkkolaskuoperaattoritIds.orSome([])} />
      </div>
    </div>
  </div>
  <div class="flex -mx-4 mt-20">
    <div class="px-4">
      <Button type={'submit'} text={$_('tallenna')} />
    </div>
    <div class="px-4">
      <Button
        on:click={cancel}
        text={$_('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
