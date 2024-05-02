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
  <H1 text="Dataskyddsbeskrivning Energicertifikatregistret" />

  <H2 text="Henkilötietojen käyttötarkoitus ja käsittelyperuste" />

  <p>
    Personuppgifterna insamlas med stöd av 18 § i lagen om energicertifikat för
    byggnader (50/2013) och 3 § i lagen om informationssystemet för byggnaders
    energicertifikat (147/2015).
  </p>

  <p>
    Aras lagstadgade uppgift är att övervaka energicertifikat och deras
    upprättare. I registret behandlas personuppgifter som behövs för att
    underhålla energicertifikatregistret.
  </p>

  <H2 text="Vilka personuppgifter behandlas?" />

  <p>
    I Energicertifikatregistret behandlar vi personuppgifterna för upprättare av
    energicertifikat:
  </p>

  <ul>
    <li>namn</li>
    <li>personbeteckning</li>
    <li>e-postadress</li>
    <li>adress</li>
    <li>land</li>
    <li>kompetensnivå</li>
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
    Vi behandlar även uppgifter som betraktas som personuppgifter såsom
    byggnadens exakta gatuadress (under 2 bostads småhus) och dess tillhörande
    permanenta byggnadsbeteckning.
  </p>

  <H2 text="Behandling, överföring och utlämnande av personuppgifter" />

  <ol class="alphabet-list">
    <li>
      Manuellt material<br />
      Manuellt material behandlas av personer som utbildats i informationssäkerhet.
      Uppgifterna i registret är skyddade mot obehörig åtkomst, ändring och förstöring.
    </li>
    <li>
      Behandling av digitalt material<br />
      Uppgifterna i registret är skyddade från obehöriga, ändringar och förlust.
      Skyddet grundar sig i hantering av användarrättigheter, tekniskt skydd av databaser
      och servrar, skydd av fysiska utrymmen, passerkontroll, skydd av datakommunikation
      samt säkerhetskopiering av uppgifter.
    </li>
  </ol>

  <p>
    Rättigheter för tillträde och behandling av uppgifter beviljas utifrån
    arbetsuppgifterna. Tillträde till systemet grundar sig på personliga
    användarnamn. Datacentralerna och uppgifterna finns fysiskt inom EU.
  </p>

  <p>
    Ara har lagt ut det tekniska underhållet och utvecklandet av
    Energicertifikatregistret på en IT-tjänsteleverantör med vilken det har
    ingåtts ett avtal om användning av personuppgifter enligt
    dataskyddsförordningen.
  </p>

  <p>
    Uppgifter får lämnas ut endast inom de gränser som föreskrivs och tillåts i
    gällande lagstiftning eller med den personuppgiftsansvariges samtycke. För
    fakturering lämnas uppgifter ut till statens gemensamma system för ekonomi-
    och personalförvaltning Kieku.
  </p>

  <p>
    Uppgifter överförs inte utanför EU eller utanför EES eller till
    internationella organisationer.
  </p>

  <H2 text="Förvaringstid för personuppgifter" />

  <p>
    Lagringsperioderna för personuppgifter i Energicertifikatregistret grundar
    sig i lagstiftning och Aras informationsstyrningsplan.
  </p>

  <H2 text="Den registrerades rättigheter" />

  <p>
    Den registrerade har rätt att få den personuppgiftsansvariges bekräftelse
    att personuppgifter som berör hen behandlas eller inte behandlas, och om
    dessa personuppgifter behandlas, har hen rätt att få tillträde till
    personuppgifterna. Uppgifterna i registret används inte för profilering
    eller automatiserat beslutsfattande. Om det har gått under ett år från att
    den registrerade använt sin rätt att granska, kan den
    personuppgiftsansvarige ta ut en avgift för de administrativa kostnader som
    orsakas av att lämna ut uppgifterna.
  </p>

  <p>
    Ett elektroniskt undertecknat energicertifikat kan inte ändras. Om
    energicertifikatet innehåller felaktiga personuppgifter kan den
    personuppgiftsansvarige underrättas om det, och den personuppgiftsansvarige
    ska förbjuda användningen av certifikatet.
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
    <li>Telefon: 029 525 0800 (växel)</li>
    <li>E-post: kirjaamo.ara(at)ara.fi</li>
  </ul>

  <H2 text="Kontaktperson i ärenden som gäller registret" />
  <ul class="contact-info">
    <li>Kirsi Unhonen, energiexpert</li>
    <li>Finansierings- och utvecklingscentralen för boendet (Ara)</li>
    <li>förnamn.efternamn(at)ara.fi</li>
  </ul>
</div>
