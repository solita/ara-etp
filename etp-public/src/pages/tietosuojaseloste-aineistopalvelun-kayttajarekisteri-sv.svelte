<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  let component;
  announceAssertively(
    $_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')
  );

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    list-style: disc;
    @apply my-4 ml-6;
  }

  ol {
    @apply my-4 ml-6;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_(
    'TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI'
  )}"
  descriptionFi={$locale === 'fi'
    ? $_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')
    : undefined}
  descriptionSv={$locale === 'sv'
    ? $_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')
    : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <H2>{$_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')}</H2>

      <H3>Syftet med och grunden för behandlingen av personuppgifter</H3>

      <p>
        Behandlingen av personuppgifter grundar sig på ett avtal mellan Ara och
        datatillståndshavaren (kunden) om användningen av Materialtjänsten. I
        registret behandlas personuppgifterna för kundens tekniska
        kontaktperson. Uppgifterna används för att skapa en
        gränssnittsförbindelse till kundens datanät samt för att lösa
        problemsituationer. Dessutom används uppgifterna för att administrera
        och följa upp behörigheter samt för att samla in logguppgifter.
      </p>

      <H3>Vilka personuppgifter behandlas?</H3>

      <p>
        I registret behandlar vi personuppgifterna för kundens tekniska
        kontaktperson:
      </p>

      <ul>
        <li>namn</li>
        <li>e-postadress</li>
        <li>företag</li>
        <li>
          den IP-adress dit informationsmaterialet överlåts via gränssnittet.
        </li>
      </ul>

      <p>Personuppgifterna fås från kunden.</p>

      <H3>Behandling, överföring och utlämnande av personuppgifter</H3>

      <p>
        Uppgifterna i registret är skyddade mot obehörig åtkomst, ändring och
        förlust. Skyddet grundar sig på behörighetshantering samt tekniska
        skyddsmetoder som skyddar informationssystemen. Rättigheter för
        tillträde och behandling av uppgifter baserar sig på arbetsuppgifterna.
        Tillträde till systemet grundar sig på personliga användarnamn.
        Utrymmena för utrustning och uppgifterna finns fysiskt inom EU.
      </p>

      <p>
        Uppgifterna lämnas i regel inte ut. Uppgifter kan lämnas ut endast i de
        fall som den gällande lagstiftningen förpliktar till.
      </p>

      <p>
        Ara har lagt ut energicertifikatregistret och det tillhörande tekniska
        underhållet och utvecklingen av Materialtjänsten åt en extern
        IT-tjänsteleverantör genom ett avtal enligt dataskyddsförordningen
        angående behandlingen av personuppgifter.
      </p>

      <p>
        Uppgifter överförs inte utanför EU eller utanför EES eller till
        internationella organisationer.
      </p>

      <H3>Förvaringstid för personuppgifter</H3>

      <p>
        Personuppgifterna förvaras under giltighetstiden för Aras och kundens
        avtal. Om den tekniska kontaktpersonen ändrar under avtalsperioden läggs
        den nya tekniska kontaktpersonen till i registret och föregångarens
        uppgifter raderas ur registret.
      </p>

      <H3>Den registrerades rättigheter</H3>

      <p>
        Den registrerade har rätt att få den personuppgiftsansvariges
        bekräftelse att personuppgifter som berör hen behandlas eller inte
        behandlas, och om dessa personuppgifter behandlas, har hen rätt att få
        tillträde till personuppgifterna.
      </p>

      <p>
        Uppgifterna i registren används inte för profilering eller automatiserat
        beslutsfattande.
      </p>

      <p>
        Om det har gått under ett år från att den registrerade använt sin rätt
        att granska, kan den personuppgiftsansvarige ta ut en avgift för de
        administrativa kostnader som orsakas av att lämna ut uppgifterna.
      </p>

      <p>
        Om registret innehåller felaktiga personuppgifter kan den registrerade
        begära rättelse av den personuppgiftsansvarige, som rättar uppgifterna.
      </p>

      <p>
        Den registrerade kan begära att hens uppgifter raderas om hen inte
        längre fungerar som teknisk kontaktperson på grund av ändringar i
        arbetsuppgifterna eller av någon annan orsak.
      </p>

      <p>
        Den registrerade har rätt att lämna in klagomål till dataombudsmannen,
        om hen ser att behandlingen av personuppgifterna bryter mot EU:s
        dataskyddsförordning.
      </p>

      <p>Kontaktuppgifter för dataombudsmannens byrå:</p>

      <ul class="contact-info">
        <li>PB 800, 00531 Helsingfors</li>
        <li>E-post (registratorskontoret): tietosuoja(at)om.fi</li>
        <li>Telefonväxel: 029 566 6700</li>
        <li>Registratorskontor: 029 566 6768</li>
      </ul>

      <H3>Personuppgiftsansvarig</H3>

      <ul class="contact-info">
        <li>Finansierings- och utvecklingscentralen för boendet (Ara)</li>
        <li>PB 30, 15141 Lahtis</li>
        <li>Telefon: 029 525 0800 (växel)</li>
        <li>E-post: kirjaamo.ara(at)ara.fi</li>
      </ul>

      <H3>Kontaktperson i ärenden som gäller registret</H3>
      <ul class="contact-info">
        <li>Kirsi Unhonen, energiexpert</li>
        <li>Finansierings- och utvecklingscentralen för boendet (Ara)</li>
        <li>förnamn.efternamn(at)ara.fi</li>
      </ul>
    </div>
  </div>
</Container>
