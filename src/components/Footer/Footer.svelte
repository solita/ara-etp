<script>
  import Version from '@Component/Version/Version';
  import { locale, _ } from '@Language/i18n';

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
          <a class="font-bold" href="tel:0295250800"> 029 525 0800 </a>
          <span class="font-bold mt-2"> {$_('footer.email')}: </span>
          <a class="font-bold" href="mailto:energiatodistus@ara.fi"
            >energiatodistus@ara.fi
          </a>
        </div>
      </div>
    </div>
    <div class="flex flex-col space-y-2">
      <span class="uppercase font-bold text-secondary">
        {$_('footer.about')}
      </span>
      {#if $locale.startsWith('sv')}
        <a
          class="font-bold text-primary"
          href="pdf/Dataskyddsbeskrivning_Energicertifikatregistret.pdf"
          target="_blank">
          Dataskyddsbeskrivning Energicertifikatregistret (pdf)
        </a>
        <a
          class="font-bold text-primary"
          href="pdf/Dataskyddsbeskrivning_Register_för_övervakningsuppgifter_om_energicertifikat.pdf"
          target="_blank">
          Dataskyddsbeskrivning Register för övervakningsuppgifter om
          energicertifikat (pdf)
        </a>
        <a
          class="font-bold text-primary"
          href="pdf/Dataskyddsbeskrivning_Register_för_upprättare_av_energiferticikat.pdf"
          target="_blank">
          Dataskyddsbeskrivning Register för upprättare av energiferticikat
          (pdf)
        </a>
      {:else}
        <a
          class="font-bold text-primary"
          href="pdf/Tietosuojaseloste_Energiatodistusrekisteri.pdf"
          target="_blank">
          Tietosuojaseloste Energiatodistusrekisteri (pdf)
        </a>
        <a
          class="font-bold text-primary"
          href="pdf/Tietosuojaseloste_Energiatodistusten_laatijarekisteri.pdf"
          target="_blank">
          Tietosuojaseloste Energiatodistusten laatijarekisteri (pdf)
        </a>
        <a
          class="font-bold text-primary"
          href="pdf/Tietosuojaseloste_Energiatodistusrekisteri.pdf"
          target="_blank">
          Tietosuojaseloste Energiatodistusten valvontatietorekisteri (pdf)
        </a>
      {/if}
    </div>
  </div>
  <div
    class="border-t border-tableborder mx-8 pt-4 pb-16 flex items-center justify-between">
    <img class="w-20 h-20" src="images/ara_logo_green.svg" alt="Ara" />
    <Version {version} />
  </div>
</footer>
