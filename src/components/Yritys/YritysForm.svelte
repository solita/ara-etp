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
  import Input2 from '@Component/Input/Input2';
  import Button from '@Component/Button/Button';

  import { countryStore, flashMessageStore } from '@/stores';

  const update = fn => (yritys = fn(yritys));

  export let submit;
  export let existing = false;

  export let yritys;

  const originalYritys = R.clone(yritys);

  const formTransformers = YritysUtils.formTransformers();
  const formValidators = YritysUtils.formValidators();

  const countryFuture = R.compose(
    Future.coalesce(Either.Left, Either.Right),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/countries/');

  $: Either.isRight($countryStore) ||
    Future.fork(countryStore.set, countryStore.set, countryFuture);

  const parseCountry = R.compose(
    R.map(R.prop('id')),
    R.chain(Maybe.toEither(R.applyTo('country-not-found'))),
    Either.leftMap(R.always(R.applyTo('connection-failure'))),
    fn => $countryStore.map(fn),
    country.findCountry
  );

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
    R.reduce(R.and, true),
    R.values,
    YritysUtils.validateYritys(formValidators)
  )(yritys);
</script>

<style>

</style>

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
          value={yritys.ytunnus}
          transform={formTransformers.ytunnus}
          validation={formValidators.ytunnus}
          update={R.compose( update, R.set(R.lensProp('ytunnus')) )}
          disabled={existing} />
      </div>
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input2
          id={'nimi'}
          name={'nimi'}
          label={$_('yritys.nimi')}
          required={true}
          bind:model={yritys}
          parse={formTransformers.nimi}
          validators={[validation.isRequired, validation.minLengthConstraint(2), validation.maxLengthConstraint(200)]}
          i18n={$_}
          lens={R.lensProp('nimi')} />
      </div>
    </div>
    <div class="py-4">
      <Input
        id={'wwwosoite'}
        name={'wwwosoite'}
        label={$_('yritys.www-osoite')}
        required={false}
        type={'url'}
        value={Maybe.fold('', R.identity, yritys.wwwosoite)}
        transform={formTransformers.wwwosoite}
        validation={formValidators.wwwosoite}
        update={R.compose( update, R.compose( R.set(R.lensProp('wwwosoite')), Maybe.fromEmpty ) )} />
    </div>
  </div>
  <div class="mt-8">
    <H1 text={$_('yritys.laskutusosoite')} />
    <div class="flex flex-col">
      <div class="py-4">
        <Input
          id={'jakeluosoite'}
          name={'jakeluosoite'}
          label={$_('yritys.jakeluosoite')}
          required={true}
          value={yritys.jakeluosoite}
          transform={formTransformers.jakeluosoite}
          validation={formValidators.jakeluosoite}
          update={R.compose( update, R.set(R.lensProp('jakeluosoite')) )} />
      </div>
      <div class="flex lg:flex-row flex-col py-4 -mx-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'postinumero'}
            name={'postinumero'}
            label={$_('yritys.postinumero')}
            required={true}
            value={yritys.postinumero}
            transform={formTransformers.postinumero}
            validation={formValidators.postinumero}
            update={R.compose( update, R.set(R.lensProp('postinumero')) )} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'postitoimipaikka'}
            name={'postitoimipaikka'}
            label={$_('yritys.postitoimipaikka')}
            required={true}
            value={yritys.postitoimipaikka}
            transform={formTransformers.postitoimipaikka}
            validation={formValidators.postitoimipaikka}
            update={R.compose( update, R.set(R.lensProp('postitoimipaikka')) )} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Autocomplete items={countryNames}>
            <Input2
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
        id={'verkkolaskuosoite'}
        name={'verkkolaskuosoite'}
        label={$_('yritys.ovt-tunnus')}
        value={Maybe.fold('', R.identity, yritys.verkkolaskuosoite)}
        transform={formTransformers.verkkolaskuosoite}
        validation={formValidators.verkkolaskuosoite}
        update={R.compose( update, R.compose( R.set(R.lensProp('verkkolaskuosoite')), Maybe.fromEmpty ) )} />
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
