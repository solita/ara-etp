<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import Link from '@Component/link';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  let component;
  announceAssertively($_('SAAVUTETTAVUUSSELOSTE'));

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    list-style: disc;
    @apply my-4;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('SAAVUTETTAVUUSSELOSTE')}"
  descriptionFi={$locale == 'fi' ? $_('SAAVUTETTAVUUSSELOSTE') : undefined}
  descriptionSv={$locale == 'sv' ? $_('SAAVUTETTAVUUSSELOSTE') : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <H2>{$_('SAAVUTETTAVUUSSELOSTE')}</H2>
      <p>
        Varke strävar efter att garantera tillgängligheten till tjänsten
        energiatodistusrekisteri.fi i enlighet med lagen om tillhandahållande av
        digitala tjänster 306/2019.
      </p>
      <p>
        Denna tillgänglighetsbeskrivning gäller webbplatsen
        energiatodistusrekisteri.fi.
      </p>
      <H3>Status för tillgängligheten</H3>
      <p>
        energiatodistusrekisteri.fi uppfyller delvis kraven på nivå AA i
        tillgänglighetsanvisningarna för webbinnehåll (WCAG 2.1).
      </p>
      <H3>Icke-tillgängligt innehåll</H3>
      <p>Innehållet nedan är inte tillgängligt av följande skäl:</p>
      <p>
        Underlåtelse att iaktta lagen om tillhandahållande av digitala tjänster
        306/2019
      </p>
      <ul class="ml-6">
        <li>
          I huvudmenyns länkar upprepar ikonens textmotsvarighet i onödan
          länktextens innehåll (WCAG 1.1.1)
        </li>
        <li>Rubrik-värdeparen har inte presenterats. (WCAG 1.3.1)</li>
        <li>
          Sökningen sker automatiskt när du lämnar sökfältet eller med Enter,
          men skärmläsaren får ingen respons. (WCAG 1.3.1)
        </li>
        <li>
          Logotypen i övre balken är i onödan inom &#60;h1&#62;-elementet. (WCAG
          1.3.1)
        </li>
        <li>
          Rubrikernas nivåer framskrider inte logiskt, flera rubriker på nivå h1
          och logiskt underställda rubriker med samma rubriknivå. (WCAG 1.3.1)
        </li>
        <li>
          Tipstexten för filtreringsvalen stängs inte genom att trycka på Esc,
          kan förbli synlig även om inriktningen flyttas någon annanstans.
          Valknapparnas funktion avviker från det man är van vid. (WCAG 1.4.13)
        </li>
        <li>
          En del av de visuella elementen skalas inte snyggt när bara texten
          förstoras. (WCAG 1.4.4)
        </li>
        <li>Fokus fungerar inte alltid logiskt. (WCAG 2.4.3)</li>
        <li>
          Blankettgruppernas synliga namntagg och tillgängliga namn stämmer inte
          alltid överens. (WCAG 2.5.3)
        </li>
        <li>
          Filtren redigerar automatiskt resultatlistan, men användaren
          informeras inte om detta på förhand. (WCAG 3.2.2)
        </li>
        <li>
          Alla sökfält är inte markerade på ett begripligt sätt. (WCAG 3.3.2)
        </li>
        <li>
          Anvisningarna för val av kompetensnivå läses inte alltid med
          skärmläsare. (WCAG 3.3.2)
        </li>
        <li>Nav-elementen har inga identifierande namn. (WCAG 4.1.2)</li>
        <li>
          Man får inte alltid respons på förflyttningen mellan sidorna med
          skärmläsningsprogrammet. (WCAG 4.1.3)
        </li>
      </ul>
      <h2>Upprättande av denna tillgänglighetsbeskrivning</h2>
      <p>
        Denna beskrivning har utarbetats 25.4.2022 och uppdaterats 27.02.2023.
      </p>
      <p>
        Utlåtandet grundar sig på en utomstående expertorganisations (Eficode
        Oy) bedömning av om webbplatsen uppfyller kraven i
        <Link
          href="https://www.finlex.fi/sv/laki/alkup/2019/20190306"
          referrerpolicy="no-referrer"
          target="_blank">
          lagen om tillhandahållande av digitala tjänster 306/2019
        </Link>.
      </p>
      <H3>Respons och kontaktuppgifter</H3>
      <p>
        Om du upptäckte en tillgänglighetsbrist som inte beskrivs på denna sida,
        vänligen berätta det för oss. Ta kontakt via responsformuläret:
        <Link
          href="https://www.varke.fi/sv/form/feedback-form"
          referrerpolicy="no-referrer"
          target="_blank">
          Ge respons (varke.fi)
        </Link>.
      </p>
      <p>
        Vi svara på ditt meddelande så snart som möjligt. Observera att det kan
        ta upp till 14 dagar innan du får vårt svar.
      </p>

      <H3>Verkställighetsförfarande</H3>

      <p>
        Transport- och kommunikationsverket Traficom övervakar att lagens
        tillgänglighetskrav följs i Finland. Om du märker brister i
        tillgängligheten på vår webbplats, vänligen kontakta oss först, det vill
        säga webbplatsens administratör.
      </p>

      <p>
        Om du inte är nöjd med det svar du har fått eller inte får något svar
        alls inom två veckor kan du lämna in en anmälan om bristande
        tillgänglighet till Transport- och kommunikationsverkets tillsyn över
        digital tillgänglighet. På Transport- och kommunikationsverkets
        webbplats finns det beskrivet hur du kan göra en anmälan och hur ärendet
        behandlas.
      </p>

      <p>
        <Link
          href="https://www.saavutettavuusvaatimukset.fi/sv/dina-rattigheter/anvandares-rattigheter"
          >Användarens rättigheter (saavutettavuusvaatimukset.fi)</Link>
      </p>

      <H3>Tillsynsmyndighetens kontaktuppgifter</H3>
      <ul class="contact-info">
        <li>Transport- och kommunikationsverket Traficom</li>
        <li>Tillsyn över digital tillgänglighet</li>
        <li>
          Webbplats: <Link href="https://tillganglighetskrav.fi" target="_blank"
            >Tillganglighetskrav.fi</Link>
        </li>
        <li>
          E-post: <Link href="mailto:tillganglighet@traficom.fi"
            >tillganglighet@traficom.fi</Link>
        </li>
        <li>Telefon: 029 534 5000 (växeln)</li>
      </ul>
    </div>
  </div>
</Container>
