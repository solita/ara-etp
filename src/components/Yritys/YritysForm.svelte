<script>
  import { _ } from '../../i18n';
  import * as Maybe from '../../utils/maybe-utils';
  import * as R from 'ramda';

  import H1 from '../H1/H1.svelte';
  import Input from '../Input/Input.svelte';
  import Button from '../Button/Button.svelte';

  export let yritys;
</script>

<style>

</style>

<form on:submit|preventDefault={console.log}>
  <div class="w-full">
    <H1 text="Perustiedot" />
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input
          id={'ytunnus'}
          name={'ytunnus'}
          label={$_('yritys.y-tunnus')}
          required={true}
          value={yritys.ytunnus} />
      </div>
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input
          id={'yritysnimi'}
          name={'yritysnimi'}
          label={$_('yritys.nimi')}
          required={true}
          value={yritys.nimi} />
      </div>
    </div>
    <div class="py-4">
      <Input
        id={'wwwosoite'}
        name={'wwwosoite'}
        label={$_('yritys.www-osoite')}
        required={false}
        type={'url'}
        value={Maybe.fold('', R.identity, yritys.wwwosoite)} />
    </div>
    <H1 text={$_('yritys.laskutusosoite')} />
    <div class="flex flex-col -my-4">
      <div class="py-4">
        <Input
          id={'jakeluosoite'}
          name={'jakeluosoite'}
          label={$_('yritys.jakeluosoite')}
          required={true}
          value={yritys.jakeluosoite} />
      </div>
      <div class="flex lg:flex-row flex-col py-4 -mx-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'postinumero'}
            name={'postinumero'}
            label={$_('yritys.postinumero')}
            required={true}
            value={yritys.postinumero}
            validation={R.test(/^[0-9]{5}$/)} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'postitoimipaikka'}
            name={'postitoimipaikka'}
            label={$_('yritys.postitoimipaikka')}
            required={true}
            value={yritys.postitoimipaikka} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'maa'}
            name={'maa'}
            label={$_('yritys.maa')}
            required={true}
            value={yritys.maa} />
        </div>
      </div>
    </div>
    <H1 text={$_('yritys.verkkolaskuosoite')} />
    <div class="lg:w-1/4 w-full">
      <Input
        id={'verkkolaskuosoite'}
        name={'verkkolaskuosoite'}
        label={$_('yritys.ovt-tunnus')}
        value={Maybe.fold('', R.identity, yritys.verkkolaskuosoite)} />
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
