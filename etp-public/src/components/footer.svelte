<script>
  import { styles as buttonStyles } from '@Component/button';
  import ButtonLink, {
    styles as buttonLinkStyles
  } from '@Component/buttonlink';
  import Container, { styles as containerStyles } from '@Component/container';
  import Link from '@Component/link';
  import IconLogin from '@Asset/icons/login-light.svg';
  import IconChat from '@Asset/icons/chat.svg';
  import ImgFooterLogoFi from '@Asset/YM_Varke_vaaka_sin_FI_RGB.png?width=1471&height=257';
  import ImgFooterLogoSv from '@Asset/YM_Varke_vaaka_sin_SV_RGB.png?width=1471&height=257';
  import ImgFooterLogoSmallFi from '@Asset/YM_Varke_kapea_sin_FI_RGB.png?width=659&height=302';
  import ImgFooterLogoSmallSv from '@Asset/YM_Varke_kapea_sin_SV_RGB.png?width=659&height=302';

  import { _, locale } from '@Localization/localization';

  export let config;

  $: logo = $locale === 'fi' ? ImgFooterLogoFi : ImgFooterLogoSv;
  $: logoSmall = $locale === 'fi' ? ImgFooterLogoSmallFi : ImgFooterLogoSmallSv;
</script>

<style>
  @media screen and (min-width: 768px) {
    .footer-logo {
      height: 150px;
    }
  }
  @media print {
    footer {
      display: none;
    }
  }
</style>

<Container {...containerStyles.grey}>
  <footer class="flex flex-col bg-grey px-2 sm:px-16 py-4 sm:py-16">
    <article class="flex flex-col md:flex-row md:my-0 md:space-x-10">
      <section class="md:w-1/3 py-4 md:py-0">
        <h2 class="mb-4">{$_('FOOTER_LAKI')}</h2>
        <p>{$_('FOOTER_VARKE_VIRANOMAINEN')}</p>
        <br />
        <Link href="https://www.varke.fi">{$_('FOOTER_VARKE_LINKKI')}</Link>
      </section>
      <section class="md:w-1/3 flex flex-col justify-between py-4 md:py-0">
        <h2 class="mb-4">{$_('FOOTER_YHTEYSTIEDOT')}</h2>
        <address class="not-italic">
          <span>{$_('EMAIL')}:</span>
          <Link href="mailto:varke.ym@gov.fi">varke.ym@gov.fi</Link>
        </address>
        <address class="not-italic flex flex-col">
          <span>{$_('FOOTER_OSOITE_1')}</span>
          <span>PL 35</span>
          <span>00023 Valtioneuvosto</span>
        </address>
        <address class="not-italic">
          <span>{$_('PUHELINVAIHDE')}:</span>
          <Link href="tel:029516001">0295 16001</Link>
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
            href={$_('FOOTER_LAHETA_PALAUTETTA_LINK')}
            {...buttonStyles.green}>
            <img
              src={IconChat}
              alt={$_('FOOTER_ICON_ALT_PALAUTE')}
              class="h-6" />
            <span>{$_('FOOTER_LAHETA_PALAUTETTA')}</span>
          </ButtonLink>
        </section>

        <section class="mt-12">
          <h2 class="mb-4">{$_('FOOTER_TIETOJENLUOVUTUS')}</h2>
          <a
            href="/tietojenluovutus-ja-rajapinnat#top"
            class="block underline text-darkgreen">
            {$_('FOOTER_TIETOJENLUOVUTUS')}
          </a>
        </section>
      </div>
    </article>

    <div class="my-16 border-t-2 border-gray-400 pt-3">
      <a href="https://www.varke.fi">
        <picture>
          <source media="(max-width: 767px)" srcset={logoSmall} />
          <source media="(min-width: 768px)" srcset={logo} />
          <img
            class="mt-8 footer-logo"
            src={logo}
            alt={$_('FOOTER_VARKE_LOGO_ALT')} />
        </picture>
      </a>
    </div>
  </footer>
</Container>
