<script>
  import { _ } from '../../i18n';

  import NavigationTabBar from '../NavigationTabBar/NavigationTabBar.svelte';
  import H1 from '../H1/H1.svelte';
  import Input from '../Input/Input.svelte';
  import Button from '../Button/Button.svelte';

  $: links = [
    { text: $_('yritys.uusi_yritys') },
    { text: $_('yritys.laatijat') }
  ];
</script>

<style type="text/postcss">
  .content {
    @apply flex flex-col -my-4 pb-8;
  }

  .content > * {
    @apply py-8;
  }
</style>

<section class="content">
  <div class="w-full">
    <NavigationTabBar {links} />
  </div>
  <div class="xl:w-10/12 w-full">
    <H1 text="Perustiedot" />
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input id={'companyid'} label={$_('yritys.y-tunnus')} required={true} />
      </div>
      <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
        <Input id={'companyname'} label={$_('yritys.nimi')} required={true} />
      </div>
    </div>
    <div class="py-4">
      <Input
        id={'wwwaddress'}
        label={$_('yritys.www-osoite')}
        required={false} />
    </div>
    <H1 text={$_('yritys.laskutusosoite')} />
    <div class="flex flex-col -my-4">
      <div class="py-4">
        <Input id={'billingaddress'} label={$_('yritys.katuosoite')} />
      </div>
      <div class="flex lg:flex-row flex-col py-4 -mx-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input id={'postnumber'} label={$_('yritys.postinumero')} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            id={'postarea'}
            label={$_('yritys.postitoimipaikka')}
            caret={true} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input id={'country'} label={$_('yritys.maa')} caret={true} />
        </div>
      </div>
    </div>
    <H1 text={$_('yritys.verkkolaskutusosoite')} />
    <div class="lg:w-1/4 w-full">
      <Input id={'networkbillingaddress'} label={$_('yritys.ovt-tunnus')} />
    </div>
    <div class="flex -mx-4 pt-8">
      <div class="px-4">
        <Button text={$_('peruuta')} type={'secondary'} />
      </div>
      <div class="px-4">
        <Button text={$_('tallenna')} />
      </div>
    </div>
  </div>
</section>
