<script>
  import { locale, _ } from '@Language/i18n';

  import Link from '@Component/Link/Link';

  export let version;
</script>

<footer class="flex flex-col w-full text-sm">
  <div class="flex flex-col md:flex-row justify-between p-8">
    <div class="flex flex-col text-primary mr-4">
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
            <Link bold={true} href="tel:0295250800" text={'029 525 0800'} />
          </div>
          <span class="font-bold mt-2"> {$_('footer.email')}: </span>
          <div>
            <Link
              bold={true}
              href={'mailto:energiatodistus@ara.fi'}
              text={'energiatodistus@ara.fi'} />
          </div>
        </div>
      </div>
    </div>
    <div class="flex flex-col space-y-2">
      <span class="uppercase font-bold text-secondary">
        {$_('footer.about')}
      </span>
      {#if $locale.startsWith('sv')}
        <div>
          <Link
            bold={true}
            href={'pdf/Dataskyddsbeskrivning_Energicertifikatregistret.pdf'}
            target={'_blank'}
            text={'Dataskyddsbeskrivning Energicertifikatregistret (pdf)'} />
        </div>
        <div>
          <Link
            bold={true}
            href={'pdf/Dataskyddsbeskrivning_Register_for_overvakningsuppgifter_om_energicertifikat.pdf'}
            target={'_blank'}
            text={'Dataskyddsbeskrivning Register för övervakningsuppgifter om energicertifikat (pdf)'} />
        </div>
        <div>
          <Link
            bold={true}
            href={'pdf/Dataskyddsbeskrivning_Register_for_upprattare_av_energiferticikat.pdf'}
            target={'_blank'}
            text={'Dataskyddsbeskrivning Register för upprättare av energiferticikat (pdf)'} />
        </div>
      {:else}
        <div>
          <Link
            bold={true}
            href={'pdf/Tietosuojaseloste_Energiatodistusrekisteri.pdf'}
            target={'_blank'}
            text={'Tietosuojaseloste Energiatodistusrekisteri (pdf)'} />
        </div>
        <div>
          <Link
            bold={true}
            href={'pdf/Tietosuojaseloste_Energiatodistusten_laatijarekisteri.pdf'}
            target={'_blank'}
            text={'Tietosuojaseloste Energiatodistusten laatijarekisteri (pdf)'} />
        </div>
        <div>
          <Link
            bold={true}
            href={'pdf/Tietosuojaseloste_Energiatodistusten_valvontatietorekisteri.pdf'}
            target={'_blank'}
            text={'Tietosuojaseloste Energiatodistusten valvontatietorekisteri (pdf)'} />
        </div>
      {/if}
    </div>
  </div>
  <div
    class="border-t border-tableborder mx-8 pt-4 pb-16 flex items-center justify-between">
    <img class="w-20 h-20" src="images/ara_logo_green.svg" alt="Ara" />
    {version.version}
  </div>
</footer>
