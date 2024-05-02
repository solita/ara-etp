<script>
  import { onMount } from 'svelte';
  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';

  let component;

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    @apply my-4 ml-6 list-disc;
  }

  ol {
    @apply my-4 ml-6;
  }

  .alphabet-list {
    list-style: upper-latin;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<div bind:this={component}>
  <H1
    text="Dataskyddsbeskrivning Register för övervakningsuppgifter om energicertifikat" />

  <H2 text="Syftet med och grunden för behandlingen av personuppgifter" />

  <p>
    Personuppgifterna insamlas med stöd av 18 § i lagen om energicertifikat för
    byggnader (50/2013) och 6 § i lagen om informationssystemet för byggnaders
    energicertifikat (147/2015).
  </p>

  <p>
    Aras lagstadgade uppgift är övervaka energicertifikat och deras upprättare.
    I registret behandlas personuppgifter som behövs för att underhålla
    registret för övervakningsuppgifter om energicertifikat.
  </p>

  <p>
    Ara övervakar användningen och riktigheten av energicertifikaten samt deras
    upprättarens verksamhet.
  </p>

  <H2 text="Vilka personuppgifter behandlas?" />

  <p>
    I registret för övervakningsuppgifter om energicertifikat behandlar vi
    byggnadens ägares personuppgifter:
  </p>

  <ul>
    <li>namn</li>
    <li>personbeteckning</li>
    <li>e-postadress</li>
    <li>adress</li>
    <li>ägd byggnad</li>
  </ul>

  <p>
    Vi behandlar också personuppgifterna för upprättare av energicertifikat:
  </p>

  <ul>
    <li>namn</li>
    <li>personbeteckning</li>
    <li>e-postadress</li>
    <li>adress</li>
  </ul>

  <p>
    Registret kan också innehålla sådana uppgifter som avförts ur registret för
    upprättare av energicertifikat och som behövs för tillsynen. I fråga om
    föråldrade energicertifikat kan registret innehålla adress och permanent
    byggnadsbeteckning.
  </p>

  <p>
    Personuppgifterna fås av den registrerade själv, av anmälningar om
    försäljning av bostäder, av dem som upprättar energicertifikat och av
    fastighetsförmedlingsföretag. Dessutom kan uppgifterna kontrolleras och
    kompletteras ur myndigheternas register. Den personuppgiftsansvarige har
    lagstadgad rätt att observera objektet på plats.
  </p>

  <H2 text="Behandling, överföring och utlämnande av personuppgifter" />

  <ol class="alphabet-list">
    <li>
      Manuellt material<br />
      Manuellt material behandlas av utbildad personal enligt uppgifternas skyddsnivå.
    </li>

    <li>
      Digitalt material<br />
      Uppgifterna i registret är skyddade från obehöriga, ändringar och förlust.
      Skyddet grundar sig i hantering av användarrättigheter, tekniskt skydd av databaser
      och servrar, skydd av fysiska utrymmen, passerkontroll, skydd av datakommunikation
      samt säkerhetskopiering av uppgifter.
    </li>
  </ol>

  <p>
    Rättigheter för tillträde och behandling av uppgifter beviljas utifrån
    arbetsuppgifterna. Tillträde till systemet grundar sig på personliga
    användarnamn. Anläggningsutrymmena och uppgifterna finns fysiskt i
    EU-området.
  </p>

  <p>
    Ara har lagt ut registrets tekniska underhåll och utveckling åt en extern
    IT-tjänsteleverantör genom ett avtal enligt dataskyddsförordningen angående
    behandlingen av personuppgifter.
  </p>

  <p>
    Uppgifter kan endast överlåtas enligt den gällande lagstiftningen och inom
    tillåtna gränser eller med den registrerades samtycke. Dokument i anknytning
    till övervakning kan överlåtas till granskningsobjektets
    fastighetsförmedlare, om objektet är till salu via en fastighetsförmedlare.
    Om energicertifikatet bifogas till byggnadslov, uppgifter kan överlåts till
    byggnadstillsyn.
  </p>

  <p>
    Uppgifter överförs inte utanför EU eller utanför EES eller till
    internationella organisationer.
  </p>

  <H2 text="Förvaringstid för personuppgifter" />

  <p>
    Lagringsperioderna för personuppgifter i registret för övervakningsuppgifter
    om energicertifikat grundar sig i Aras informationsstyrningsplan.
  </p>

  <H2 text="Den registrerades rättigheter" />

  <p>
    Den registrerade har rätt att få den personuppgiftsansvariges bekräftelse
    att personuppgifter som berör hen behandlas eller inte behandlas, och om
    dessa personuppgifter behandlas, har hen rätt att få tillträde till
    personuppgifterna. Den registrerade har inte själv tillgång till
    personuppgifterna, men hen kan göra en granskningsbegäran till den
    personuppgiftsansvariges representant.
  </p>

  <p>
    Uppgifterna i registret används inte för profilering eller automatiserat
    beslutsfattande.
  </p>

  <p>
    Om det har gått under ett år från att den registrerade använt sin rätt att
    granska, kan den personuppgiftsansvarige ta ut en avgift för de
    administrativa kostnader som orsakas av att lämna ut uppgifterna.
  </p>

  <p>
    Den registrerade kan begära att den personuppgiftsansvarige utan onödigt
    dröjsmål korrigerar de felaktiga personuppgifterna som berör den
    registrerade.
  </p>

  <p>
    Den registrerade har inte rätt att begära att hens uppgifter tas bort,
    eftersom behandlingen av uppgifter grundar sig i lagstiftning. På grund av
    det här har den registrerade inte heller rätt att motsätta sig behandlingen
    av personuppgifter, och har inte heller rätt att få sina uppgifter överförda
    till ett annat system. Man kan heller inte begära att behandlingen av
    personuppgifter ska begränsas, eftersom uppgifterna behandlas med stöd av
    lagen.
  </p>

  <p>
    Den registrerade har rätt att lämna in klagomål till dataombudsmannen, om
    hen ser att behandlingen av personuppgifterna bryter mot EU:s
    dataskyddsförordning.
  </p>

  <p>Kontaktuppgifter för dataombudsmannens byrå:</p>

  <ul class="contact-info">
    <li>PB 800, 00531 Helsingfors</li>
    <li>E-post (registratorskontoret): tietosuoja(at)om.fi</li>
    <li>Telefonväxel: 029 566 6700</li>
    <li>Registratorskontor: 029 566 6768</li>
  </ul>

  <H2 text="Personuppgiftsansvarig" />

  <ul class="contact-info">
    <li>Finansierings- och utvecklingscentralen för boendet (Ara)</li>
    <li>PB 30, 15141 Lahtis</li>
    <li>Tfn (växel): 029 525 0800</li>
    <li>E-post: E-post adress: kirjaamo.ara(at)ara.fi</li>
  </ul>

  <H2 text="Kontaktperson i ärenden som gäller registret" />

  <ul class="contact-info">
    <li>Kirsi Unhonen, energiexpert</li>
    <li>Finansierings- och utvecklingscentralen för boendet (Ara)</li>
    <li>fornamn.efternamn(at)ara.fi</li>
  </ul>
</div>
