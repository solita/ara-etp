<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as validation from '@Utility/validation';
  import * as YritysUtils from './yritys-utils';
  import * as country from './country';

  import Autocomplete from '../Autocomplete/Autocomplete';
  import H1 from '@Component/H1/H1';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';

  import { countryStore, flashMessageStore } from '@/stores';

  const update = fn => (yritys = fn(yritys));

  export let submit;
  export let existing = false;

  export let yritys;

  export let disabled = false;

  const originalYritys = R.clone(yritys);

  const formParsers = YritysUtils.formParsers();
  const formSchema = YritysUtils.formSchema();

  const countryFuture = R.compose(
    Future.coalesce(Either.Left, Either.Right),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/countries/');

  const parseCountry = R.compose(
    R.map(R.prop('id')),
    R.chain(Maybe.toEither(R.applyTo('country-not-found'))),
    Either.leftMap(R.always(R.applyTo('connection-failure'))),
    fn => $countryStore.map(fn),
    country.findCountry
  );

  Either.isRight($countryStore) ||
    Future.fork(countryStore.set, countryStore.set, countryFuture);

  $: Either.isRight($countryStore) ||
    R.compose(
      Future.fork(countryStore.set, countryStore.set),
      R.chain(Future.after(1000))
    )(countryFuture);

  $: countryNames = Either.foldRight(
    [],
    R.map(
      R.prop(
        `label-${R.compose(
          R.head,
          R.split('-')
        )($locale)}`
      )
    ),
    $countryStore
  );

  $: isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    R.values,
    validation.validateModelObject(formSchema)
  )(yritys);

  $: console.log("Form validation: ", isValidForm);
</script>

<form
  on:submit|preventDefault={_ => {
    if (isValidForm) {
      flashMessageStore.flush();
      submit(yritys);
    } else {
      flashMessageStore.add('Yritys', 'error', $_('yritys.messages.validation-error'));
    }
  }}>
  <div class="w-full mt-3">
    <H1 text="Perustiedot" />
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
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
          i18n={$_}/>
      </div>
    </div>
    <div class="py-4">
      <Input
        {disabled}
        id={'wwwosoite'}
        name={'wwwosoite'}
        label={$_('yritys.www-osoite')}
        required={false}
        bind:model={yritys}
        lens={R.lensProp('wwwosoite')}
        format={Maybe.orSome('')}
        parse={formParsers.wwwosoite}
        validators={formSchema.wwwosoite}
        i18n={$_}/>
    </div>
  </div>
  <div class="mt-8">
    <H1 text={$_('yritys.laskutusosoite')} />
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
      <div class="flex lg:flex-row flex-col py-4 -mx-4">
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
              parse={parseCountry}
              i18n={$_} />
          </Autocomplete>
        </div>
      </div>
    </div>
  </div>
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
  </div>
  <div class="flex -mx-4 pt-8">
    <div class="px-4">
      <Button type={'submit'} text={$_('tallenna')} />
    </div>
    <div class="px-4">
      <Button
        on:click={event => {
          event.preventDefault();
          yritys = R.clone(originalYritys);
        }}
        text={$_('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
