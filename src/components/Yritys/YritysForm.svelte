<script>
  import { _ } from '../../i18n';
  import * as Maybe from '../../utils/maybe-utils';
  import * as R from 'ramda';
  import * as YritysUtils from './yritys-utils';

  import H1 from '../H1/H1.svelte';
  import Input from '../Input/Input.svelte';
  import Button from '../Button/Button.svelte';

  const update = fn => (yritys = fn(yritys));

  export let submit;

  export let yritys;

  $: isValidForm = R.compose(
    R.reduce(R.and, true),
    R.values,
    YritysUtils.validateYritysForm(YritysUtils.formValidators)
  )(yritys);

  $: console.log(
    YritysUtils.validateYritysForm(YritysUtils.formValidators, yritys)
  );
</script>

<style>

</style>

<form
  on:submit|preventDefault={_ => {
    if (isValidForm) {
      submit(yritys);
    }
  }}>
  <div class="w-full">
    <H1 text="Perustiedot" />
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input
          id={'ytunnus'}
          name={'ytunnus'}
          label={$_('yritys.y-tunnus')}
          required={true}
          value={yritys.ytunnus}
          transform={YritysUtils.formTransformers.ytunnus}
          validation={YritysUtils.formValidators.ytunnus}
          update={R.compose( update, R.set(R.lensProp('ytunnus')) )} />
      </div>
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input
          id={'nimi'}
          name={'nimi'}
          label={$_('yritys.nimi')}
          required={true}
          value={yritys.nimi}
          transform={YritysUtils.formTransformers.nimi}
          validation={YritysUtils.formValidators.nimi}
          update={R.compose( update, R.set(R.lensProp('nimi')) )} />
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
        transform={YritysUtils.formTransformers.wwwosoite}
        validation={YritysUtils.formValidators.wwwosoite}
        update={R.compose( update, R.compose( R.set(R.lensProp('wwwosoite')), Maybe.fromEmpty ) )} />
    </div>
    <H1 text={$_('yritys.laskutusosoite')} />
    <div class="flex flex-col -my-4">
      <div class="py-4">
        <Input
          id={'jakeluosoite'}
          name={'jakeluosoite'}
          label={$_('yritys.jakeluosoite')}
          required={true}
          value={yritys.jakeluosoite}
          transform={YritysUtils.formTransformers.jakeluosoite}
          validation={YritysUtils.formValidators.jakeluosoite}
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
            transform={YritysUtils.formTransformers.postinumero}
            validation={YritysUtils.formValidators.postinumero}
            update={R.compose( update, R.set(R.lensProp('postinumero')) )} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'postitoimipaikka'}
            name={'postitoimipaikka'}
            label={$_('yritys.postitoimipaikka')}
            required={true}
            value={yritys.postitoimipaikka}
            transform={YritysUtils.formTransformers.postitoimipaikka}
            validation={YritysUtils.formValidators.postitoimipaikka}
            update={R.compose( update, R.set(R.lensProp('postitoimipaikka')) )} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'maa'}
            name={'maa'}
            label={$_('yritys.maa')}
            required={true}
            value={yritys.maa}
            transform={YritysUtils.formTransformers.maa}
            validation={YritysUtils.formValidators.maa}
            update={R.compose( update, R.set(R.lensProp('maa')) )} />
        </div>
      </div>
    </div>
    <H1 text={$_('yritys.verkkolaskuosoite')} />
    <div class="lg:w-1/4 w-full">
      <Input
        id={'verkkolaskuosoite'}
        name={'verkkolaskuosoite'}
        label={$_('yritys.ovt-tunnus')}
        value={Maybe.fold('', R.identity, yritys.verkkolaskuosoite)}
        transform={YritysUtils.formTransformers.verkkolaskuosoite}
        validation={YritysUtils.formValidators.verkkolaskuosoite}
        update={R.compose( update, R.compose( R.set(R.lensProp('verkkolaskuosoite')), Maybe.fromEmpty ) )} />
    </div>
    <div class="flex -mx-4 pt-8">
      <div class="px-4">
        <Button type={'submit'} text={$_('tallenna')} />
      </div>
      <div class="px-4">
        <Button text={$_('peruuta')} type={'reset'} style={'secondary'} />
      </div>
    </div>
  </div>
</form>
