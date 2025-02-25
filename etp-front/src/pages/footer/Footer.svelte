<script>
  import { locale, _ } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import Link from '@Component/Link/Link';

  export let version;

  $: footerLogoPath = Locales.isSV($locale)
    ? 'images/YM_Varke_vaaka_sin_SV_RGB.png'
    : 'images/YM_Varke_vaaka_sin_FI_RGB.png';
</script>

<footer class="flex flex-col w-full text-sm">
  <div class="flex flex-col md:flex-row justify-between p-2 lg:p-8">
    <div class="flex flex-col mr-4 text-ara-2021-green-small-text">
      <span class="uppercase font-bold text-secondary">
        {$_('footer.contact-info')}
      </span>
      <div class="flex space-x-4">
        <div class="flex flex-col">
          <span class="font-bold"> {$_('footer.name')} </span>
          <span class="font-bold"> {$_('footer.po-box')} </span>
          <span class="font-bold"> {$_('footer.post-num')} </span>
        </div>
        <div class="flex flex-col">
          <span class="font-bold"> {$_('footer.phone')}: </span>
          <div>
            <Link
              bold={true}
              additionalClasses="text-ara-2021-green-small-text"
              href="tel:0295250800"
              text={'029 525 0800'} />
          </div>
          <span class="font-bold mt-2"> {$_('footer.email')}: </span>
          <div>
            <Link
              bold={true}
              additionalClasses="text-ara-2021-green-small-text"
              href={'mailto:varke.ym@gov.fi'}
              text={'varke.ym@gov.fi'} />
          </div>
        </div>
      </div>
    </div>
    <div class="flex flex-col space-y-2">
      <span class="uppercase font-bold text-secondary">
        {$_('footer.about')}
      </span>
      <div>
        <Link
          bold={true}
          additionalClasses="text-ara-2021-green-small-text"
          href={'#/saavutettavuusseloste'}
          text={$_('footer.accessibility-report')} />
      </div>
      <div>
        <Link
          bold={true}
          additionalClasses="text-ara-2021-green-small-text"
          href={'#/tietosuojaseloste-energiatodistusrekisteri'}
          text={$_('footer.tietosuojaseloste-energiatodistusrekisteri')} />
      </div>
      <div>
        <Link
          bold={true}
          additionalClasses="text-ara-2021-green-small-text"
          href={'#/tietosuojaseloste-laatijarekisteri'}
          text={$_('footer.tietosuojaseloste-laatijarekisteri')} />
      </div>
      <div>
        <Link
          bold={true}
          additionalClasses="text-ara-2021-green-small-text"
          href={'#/tietosuojaseloste-valvontatietorekisteri'}
          text={$_('footer.tietosuojaseloste-valvontatietorekisteri')} />
      </div>
    </div>
  </div>
  <div
    class="border-t border-tableborder mx-8 pt-4 pb-16 flex items-center justify-between">
    <img class="h-32" src={footerLogoPath} alt="Varke" />
    <div class="">
      {version.version}
    </div>
  </div>
</footer>
