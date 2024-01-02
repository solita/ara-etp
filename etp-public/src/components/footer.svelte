<script>
  import { styles as buttonStyles } from '@Component/button';
  import ButtonLink, {
    styles as buttonLinkStyles
  } from '@Component/buttonlink';
  import Container, { styles as containerStyles } from '@Component/container';
  import Link from '@Component/link';
  import IconLogin from '@Asset/icons/login-light.svg';
  import IconChat from '@Asset/icons/chat.svg';
  import ImgLogoBlack from '@Asset/ara_logo_black.png';
  import ImgLogoBlackSwe from '@Asset/ara_swe_logo_black.png';

  import { _, locale } from '@Localization/localization';

  export let config;

  $: logo = $locale === 'fi' ? ImgLogoBlack : ImgLogoBlackSwe;
</script>

<style>
  @media print {
    footer {
      display: none;
    }
  }

  .logo {
    height: 140px;
    width: auto;
  }
</style>

<Container {...containerStyles.grey}>
  <footer class="flex flex-col bg-grey px-2 sm:px-16 py-4 sm:py-16">
    <article class="flex flex-col md:flex-row md:my-0 md:space-x-10">
      <section class="md:w-1/3 py-4 md:py-0">
        <h2 class="mb-4">{$_('FOOTER_LAKI')}</h2>
        <p>{$_('FOOTER_ARA_VIRANOMAINEN')}</p>
        <br />
        <Link href="https://www.ara.fi">{$_('FOOTER_ARA_LINKKI')}</Link>
      </section>
      <section class="md:w-1/3 flex flex-col justify-between py-4 md:py-0">
        <h2 class="mb-4">{$_('FOOTER_YHTEYSTIEDOT')}</h2>
        <address class="not-italic">
          <span>{$_('EMAIL')}:</span>
          <Link href="mailto:energiatodistus@ara.fi"
            >energiatodistus@ara.fi</Link>
        </address>
        <address class="not-italic flex flex-col">
          <span>{$_('FOOTER_OSOITE_1')}</span>
          <span>PL 30</span>
          <span>15141 LAHTI</span>
        </address>
        <address class="not-italic">
          <span>{$_('PUHELINVAIHDE')}:</span>
          <Link href="tel:0295250800">029 525 0800</Link>
        </address>
        <a href="/tietoa-sivustosta" class="block underline text-darkgreen">
          {$_('TIETOA_SIVUSTOSTA')}
        </a>
      </section>
      <div class="md:w-1/3 flex flex-col py-4 md:py-0">
        <section>
          <h2 class="mb-4">{$_('LAATIJOIDEN_PALVELU')}</h2>
          {#if config}
            <ButtonLink
              href={config.privateSiteUrl}
              {...buttonLinkStyles.ashblue}>
              <img
                src={IconLogin}
                alt={$_('FOOTER_ICON_ALT_LOGIN')}
                class="h-6" />
              <span>{$_('KIRJAUDU')}</span>
            </ButtonLink>
          {/if}
        </section>
        <section class="py-4">
          <h2 class="mb-4">{$_('FOOTER_PALAUTE')}</h2>
          <ButtonLink
            href={'https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Anna_palautetta(1280)?r=31603'}
            {...buttonStyles.green}>
            <img
              src={IconChat}
              alt={$_('FOOTER_ICON_ALT_PALAUTE')}
              class="h-6" />
            <span>{$_('FOOTER_LAHETA_PALAUTETTA')}</span>
          </ButtonLink>
        </section>

        <section class="mt-12">
          <h2 class="mb-4">{$_('FOOTER_AINEISTOPALVELU')}</h2>
          <a
            href="/aineistopalvelu-liittyminen#top"
            class="block underline text-darkgreen">
            {$_('AINEISTOPALVELU_LIITTYMINEN')}
          </a>
        </section>
      </div>
    </article>

    <div class="my-16 border-t-2 border-gray-400 pt-3">
      <a href="https://www.ara.fi">
        <img class="logo pt-8" src={logo} alt={$_('FOOTER_ARA_LOGO_ALT')} />
      </a>
    </div>
  </footer>
</Container>
