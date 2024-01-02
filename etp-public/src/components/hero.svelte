<script>
  import Logo from '@Asset/logo.svg';

  import Button, { styles as buttonStyles } from '@Component/button';
  import Container, { styles as containerStyles } from '@Component/container';

  import { _ } from '@Localization/localization';

  export let buttonClick;
</script>

<style>
  .hero {
    height: 555px;
    background-image: url('@Asset/hero.jpg?{fit:"inside",height:1000,width:1440}');
    background-position: right 0 bottom -150px;
  }

  .title-text {
    font-size: 1.1em;
  }

  @media screen and (max-width: 1024px) {
    .hero {
      background-position: left -200px bottom -150px;
    }
  }

  @media screen and (max-width: 640px) {
    .hero {
      height: inherit;
      background-image: none;
    }
  }
</style>

<Container {...containerStyles.hero}>
  <div class="hero flex items-center" role="presentation">
    <section
      class="w-full xl:w-1/3 lg:w-5/12 sm:w-7/12 bg-white text-ashblue sm:mx-16 py-8
      px-2 sm:px-12">
      <div class="items-center mb-8 hidden sm:flex">
        <img src={Logo} alt={$_('HEADER_ARA_LOGO_ALT')} class="h-14" />
        <h2 class="pl-2 font-bold title-text">
          {$_('ENERGIATODISTUSREKISTERI')}
        </h2>
      </div>
      <strong class="block mb-8">{$_('HERO_DESCRIPTION')}</strong>
      <Button {...buttonStyles.green} on:click={buttonClick}>
        {$_('ALOITA')}
      </Button>
    </section>
  </div>
</Container>
