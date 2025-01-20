<script>
  import { onMount } from 'svelte';
  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Link from '@Component/Link/Link';

  let component;

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    @apply my-4 list-disc;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<div bind:this={component}>
  <H1 text="Tillgänglighetsbeskrivning" />
  <p>
    ARA strävar efter att garantera tillgängligheten till
    private.energiatodistusrekisteri.fi i enlighet med lagen om
    tillhandahållande av digitala tjänster 306/2019.
  </p>
  <p>
    Denna tillgänglighetsbeskrivning gäller webbplatsen
    private.energiatodistusrekisteri.fi.
  </p>

  <H2 text="Status för tillgängligheten" />
  <p>
    private.energiatodistusrekisteri.fi uppfyller delvis kraven på nivå AA i
    tillgänglighetsanvisningarna för webbinnehåll (WCAG 2.1).
  </p>

  <H2 text="Icke-tillgängligt innehåll" />
  <p>Innehållet nedan är inte tillgängligt av följande skäl:</p>

  <p>
    Underlåtelse att iaktta lagen om tillhandahållande av digitala tjänster
    306/2019
  </p>

  <ul class="ml-6">
    <li>Vissa ikoner har textmotsvarighet på engelska. (WCAG 1.1.1)</li>
    <li>
      Textens formateringsknappar har inget tillgängligt namn. (WCAG 1.1.1)
    </li>
    <li>
      Navigeringarna är inte konsekvent markerade med landmärken. Sidorna saknar
      Main-landmärke. (WCAG 1.3.1, 2.4.1)
    </li>
    <li>
      Innehåll som inte är en rubrik har markerats som rubrik. (WCAG 1.3.1)
    </li>
    <li>
      Aktiva element, såsom den valda fliken eller den valda sidan i
      sidnavigeringen, har inte markerats. (WCAG 1.3.1)
    </li>
    <li>
      Det programmässiga genomförandet motsvarar ställvis inte sidans visuella
      utseende. (WCAG 1.3.1)
    </li>
    <li>
      Namntaggarna på alla sökfält (label) har inte kopplats till fälten. (WCAG
      1.3.1)
    </li>
    <li>
      Skärmläsaren får inte information om ändringar i sidans innehåll. (WCAG
      1.3.1)
    </li>
    <li>
      I tjänsten finns blanketter där man begär uppgifter om användaren, men
      fältens exakta syfte har inte fastställts. (WCAG 1.3.5)
    </li>
    <li>Status för vissa knappar är endast markerade med färg. (WCAG 1.4.1)</li>
    <li>Webbplatsen skalas inte till en liten skärm. (WCAG 1.4.10)</li>
    <li>
      Användargränssnittselementens kontraster är inte alltid tillräckliga.
      (WCAG 1.4.11)
    </li>
    <li>Kontrasterna i texterna är inte alltid tillräckliga. (WCAG 1.4.3)</li>
    <li>
      En del av sidans element får inte tangentfokus eller skärmläsarens fokus.
      (WCAG 2.1.1)
    </li>
    <li>
      Fokusordningen är inte alltid logisk. Fokusen överförs inte till modalen.
      (WCAG 2.4.3)
    </li>
    <li>På sidorna finns flera rubriker på nivå h1. (WCAG 2.4.6)</li>
    <li>
      Tangentbordsfokus syns inte alltid eller visas inte felaktigt. (WCAG
      2.4.7)
    </li>
    <li>Vissa knappar har engelskt attribut. (WCAG 3.1.2)</li>
    <li>Länken Företag byter plats i navigeringen. (WCAG 3.2.3)</li>
    <li>
      De obligatoriska fälten är markerade med en asteriskikon, men ikonens
      betydelse har inte förklarats. (WCAG 3.3.2)
    </li>
    <li>Rollen för rullgardinsmenyerna är inte markerad. (WCAG 4.1.2)</li>
    <li>Komponentens status är inte alltid markerad. (WCAG 4.1.2)</li>
    <li>
      Skärmläsaren får ingen information om felmeddelande eller om sparandet har
      lyckats. (WCAG 4.1.3)
    </li>
  </ul>

  <H2 text="Upprättande av denna tillgänglighetsbeskrivning" />
  <p>Denna beskrivning har utarbetats 25.4.2022 och uppdaterats 27.02.2023.</p>
  <p>
    Utlåtandet grundar sig på en utomstående expertorganisations (Eficode Oy)
    bedömning av om webbplatsen uppfyller kraven i <Link
      href="https://www.finlex.fi/sv/laki/alkup/2019/20190306"
      text="lagen om tillhandahållande av digitala tjänster 306/2019" />.
  </p>

  <H2 text="Respons och kontaktuppgifter" />
  <p>
    Om du upptäckte en tillgänglighetsbrist som inte beskrivs på denna sida,
    vänligen berätta det för oss. Kontakta oss genom att skicka e-post till
    adressen: viestinta.ara (at) ara.fi
  </p>
  <p>
    Vi svara på ditt meddelande så snart som möjligt. Observera att det kan ta
    upp till 14 dagar innan du får vårt svar.
  </p>

  <H2 text="Verkställighetsförfarande" />
  <p>
    Transport- och kommunikationsverket Traficom övervakar att lagens
    tillgänglighetskrav följs i Finland. Om du märker brister i tillgängligheten
    på vår webbplats, vänligen kontakta oss först, det vill säga webbplatsens
    administratör.
  </p>

  <p>
    Om du inte är nöjd med det svar du har fått eller inte får något svar alls
    inom två veckor kan du lämna in en anmälan om bristande tillgänglighet till
    Transport- och kommunikationsverkets tillsyn över digital tillgänglighet. På
    Transport- och kommunikationsverkets webbplats finns det beskrivet hur du
    kan göra en anmälan och hur ärendet behandlas.
  </p>

  <p>
    <Link
      href="https://saavutettavuusvaatimukset.fi/sv/dina-rattigheter-0"
      text="Användarens rättigheter (saavutettavuusvaatimukset.fi)" />
  </p>

  <H2 text="Tillsynsmyndighetens kontaktuppgifter" />
  <ul class="contact-info">
    <li>Transport- och kommunikationsverket Traficom</li>
    <li>Tillsyn över digital tillgänglighet</li>
    <li>
      Webbplats: <Link
        href="https://tillganglighetskrav.fi"
        text="Tillganglighetskrav.fi"
        target="_blank" />
    </li>
    <li>
      E-post: <Link
        href="mailto:tillganglighet@traficom.fi"
        text="tillganglighet@traficom.fi" />
    </li>
    <li>Telefon: 029 534 5000 (växeln)</li>
  </ul>
</div>
