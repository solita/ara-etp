<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  let component;
  announceAssertively($_('TIETOSUOJASELOSTE_LAATIJAREKISTERI'));

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

  .alphabet-list {
    list-style: upper-latin;
  }

  .numbered-list {
    list-style: decimal;
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
    'TIETOSUOJASELOSTE_LAATIJAREKISTERI'
  )}"
  descriptionFi={$locale === 'fi'
    ? $_('TIETOSUOJASELOSTE_LAATIJAREKISTERI')
    : undefined}
  descriptionSv={$locale === 'sv'
    ? $_('TIETOSUOJASELOSTE_LAATIJAREKISTERI')
    : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <H2>{$_('TIETOSUOJASELOSTE_LAATIJAREKISTERI')}</H2>

      <H3>Syftet med och grunden för behandlingen av personuppgifter</H3>

      <p>
        Personuppgifterna insamlas med stöd av 18 § i lagen om energicertifikat
        för byggnader (50/2013) och 2 § i lagen om informationssystemet för
        byggnaders energicertifikat (147/2015).
      </p>

      <p>
        Varkes lagstadgade uppgift är att övervaka energicertifikat och deras
        upprättare. I registret behandlas personuppgifter som behövs för att
        underhålla registret över upprättare av energicertifikat. En del av
        uppgifterna ska vara tillgängliga genom den offentliga
        informationstjänsten. Personuppgifterna behandlas också för fakturering.
      </p>

      <H3>Vilka personuppgifter behandlas?</H3>

      <p>
        I registret för upprättare av energicertifikat behandlar vi
        personuppgifterna för upprättare av energicertifikat:
      </p>

      <ul>
        <li>namn</li>
        <li>personbeteckning</li>
        <li>telefonnummer</li>
        <li>e-postadress</li>
        <li>adress</li>
        <li>land</li>
        <li>kompetensnivå</li>
        <li>verksamhetsområde</li>
        <li>giltighet för upprättarbehörighet</li>
      </ul>

      <p>Vi behandlar också företagsuppgifter:</p>

      <ul>
        <li>namn</li>
        <li>FO-nummer</li>
        <li>adress</li>
        <li>land</li>
        <li>e-fakturaadress</li>
        <li>nätfakturerare</li>
      </ul>

      <p>
        Registret innehåller också uppgifter om de energicertifikaten som
        upprättaren upprättat.
      </p>

      <p>
        Utgångspunkten är att personuppgifter om upprättare av energicertifikat
        fås från behörighetsverifierare (FISE Oy och Kiinteistöalan
        koulutuskeskus Kiinko). Om upprättaren av energicertifikat använder
        elektronisk signatur, fås hens logguppgifterna (personbeteckning och
        klockslag för signaturfunktionerna) via Suomi.fi-autentisering.
        Företagsuppgifter och uppgifter om upprättade energicertifikaten fås av
        upprättare av energicertifikat.
      </p>

      <H3>Behandling, överföring och utlämnande av personuppgifter</H3>

      <ol class="alphabet-list">
        <li>
          Manuellt material<br />
          Manuellt material behandlas av utbildad personal enligt uppgifternas skyddsnivå.
        </li>
        <li>
          Behandling av digitalt material<br />
          Uppgifterna i registret är skyddade från obehöriga, ändringar och förlust.
          Skyddet grundar sig i hantering av användarrättigheter, tekniskt skydd
          av databaser och servrar, skydd av fysiska utrymmen, passerkontroll, skydd
          av datakommunikation samt säkerhetskopiering av uppgifter. Rättigheter
          för tillträde och behandling av uppgifter beviljas utifrån arbetsuppgifterna.
        </li>
      </ol>

      <p>
        Tillträde till systemet grundar sig på personliga användarnamn.
        Datacentralerna och uppgifterna finns fysiskt inom EU.
      </p>

      <p>
        Uppgifterna i registret används inte för profilering eller automatiserat
        beslutsfattande.
      </p>

      <p>
        Uppgifter kan endast överlåtas enligt den gällande lagstiftningen och
        inom tillåtna gränser eller med den registrerades samtycke.
      </p>

      <p>
        På Varkes webbplats kan man offentligt söka alla registrerades namn,
        verksamhetsområden och giltighet för upprättarbehörighet. Registrerade
        kan även själv göra adress, telefonnummer, e-postadress och webbplats
        offentligt tillgängliga.
      </p>

      <p>
        För autentisering överlåts personuppgifter till suomi.fi-tjänsten som en
        del av identifikationen. För fakturering lämnas uppgifter ut till
        statens gemensamma system för ekonomi- och personalförvaltning Kieku.
      </p>

      <p>
        Varke har lagt ut energicertifikatregistrets tekniska underhåll och
        utveckling åt en extern IT-tjänsteleverantör genom ett avtal enligt
        dataskyddsförordningen angående behandlingen av personuppgifter.
      </p>

      <p>
        Uppgifter överförs inte utanför EU eller utanför EES eller till
        internationella organisationer.
      </p>

      <H3>Förvaringstid för personuppgifter</H3>

      <p>
        Lagringsperioderna för personuppgifter i register för upprättare av
        energicertifikat grundar sig i lagstiftning och Varkes
        informationsstyrningsplan.
      </p>

      <H3>Den registrerades rättigheter</H3>

      <p>
        Den registrerade har rätt att få den personuppgiftsansvariges
        bekräftelse att personuppgifter som berör hen behandlas eller inte
        behandlas, och om dessa personuppgifter behandlas, har hen rätt att få
        tillträde till personuppgifterna.
      </p>

      <p>
        Den registrerade har själv tillträde till personuppgifterna via
        webbtjänsten. Om det har gått under ett år från att den registrerade
        använt sin rätt att granska, kan den personuppgiftsansvarige ta ut en
        avgift för de administrativa kostnader som orsakas av att lämna ut
        uppgifterna.
      </p>

      <p>
        Den registrerade kan själv korrigera egna uppgifter via webbtjänsten.
        Den registrerade har rätt att kräva att den personuppgiftsansvarige utan
        obefogat dröjsmål rättar felaktiga personuppgifter som berör den
        registrerade. Om det inte finns tillgång till nättjänsten, kan den
        registrerade begära omprövning hos företrädaren för den
        personuppgiftsansvarige.
      </p>

      <p>
        Enligt 7 § i lagen om informationssystemet för byggnaders
        energicertifikat, uppgifter om en upprättare av energicertifikat gallras
        ur registret över upprättare av energicertifikat
      </p>

      <ol class="numbered-list">
        <li>
          om upprättaren av energicertifikat meddelar att han eller hon upphör
          med sin verksamhet eller av någon annan anledning begär att
          uppgifterna gallras ur registret permanent eller för viss tid,
        </li>
        <li>
          när giltighetstiden för behörigheten att upprätta energicertifikat
          löper ut och den som konstaterar behörigheten inte har meddelat
          Varke att behörigheten
          förnyas,
        </li>
        <li>
          om upprättaren av energicertifikat meddelas ett förbud att upprätta
          certifikat,
        </li>
        <li>
          om upprättaren inte längre har allmänna förutsättningar för att
          bedriva verksamhet,
        </li>
        <li>
          när Varke har erhållit
          information om att upprättaren har avlidit.
        </li>
      </ol>

      <p>
        Den registrerades aktiviteter kan också övervakas efter att hens
        uppgifter har tagits bort från register för upprättare av
        energicertifikat. Då flyttas upprättarens uppgifter från registret över
        dem som upprättar certifikat till registret över tillsynsuppgifter.
      </p>

      <p>
        Den registrerade har inte rätt att motsätta sig behandlingen av
        personuppgifter, och har inte heller rätt att få sina uppgifter
        överförda till ett annat system, eftersom behandlingen av uppgifter
        grundar sig i lagstiftning. Man kan heller inte begära att behandlingen
        av personuppgifter ska begränsas, eftersom uppgifterna behandlas med
        stöd av lagen.
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
        <li>Centralen för statligt stött bostadsbyggande (Varke)</li>
        <li>PB 35, 00023 Statsrådet</li>
        <li>Telefon: 0295 16001 (växel)</li>
        <li>E-post: varke.ym(at)gov.fi</li>
      </ul>

      <H3>Kontaktperson i ärenden som gäller registret</H3>

      <ul class="contact-info">
        <li>Kimmo Huovinen, biträdande direktör</li>
        <li>Centralen för statligt stött bostadsbyggande (Varke)</li>
        <li>förnamn.efternamn(at)gov.fi</li>
      </ul>
    </div>
  </div>
</Container>
