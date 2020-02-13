<script>
  import NavigationTabBar from '../NavigationTabBar/NavigationTabBar.svelte';
  import H1 from '../H1/H1.svelte';
  import Input from '../Input/Input.svelte';
  import Button from '../Button/Button.svelte';

  const links = [{ text: 'Uusi yritys' }, { text: 'Laatijat' }];
</script>

<style>
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
  <H1 text="Perustiedot" />
  <div class="w-1/5">
    <Input id={'companyname'} label={'Nimi'} required={true} />
  </div>
  <div class="w-1/12">
    <Input id={'companyid'} label={'Y-Tunnus'} required={true} />
  </div>
  <H1 text={'Laskutusosoite'} />
  <div class="flex flex-col w-8/12 -my-4">
    <div class="py-4">
      <Input id={'billingaddress'} label={'Katuosoite'} />
    </div>
    <div class="py-4 flex flex-row -mx-4">
      <div class="w-1/3 px-4">
        <Input id={'postnumber'} label={'Postinumero'} />
      </div>
      <div class="w-1/3 px-4">
        <Input id={'postarea'} label={'Postitoimipaikka'} caret={true} />
      </div>
      <div class="w-1/3 px-4">
        <Input id={'country'} label={'Maa'} caret={true} />
      </div>
    </div>
  </div>
  <H1 text={'Verkkolaskutusosoite'} />
  <div class="w-1/4">
    <Input id={'networkbillingaddress'} label={'OVT-Tunnus'} />
  </div>
  <div class="flex -mx-4 pt-4">
    <div class="px-4">
      <Button text={'Peruuta'} type={'secondary'} />
    </div>
    <div class="px-4">
      <Button text={'Tallenna'} />
    </div>
  </div>
</section>
