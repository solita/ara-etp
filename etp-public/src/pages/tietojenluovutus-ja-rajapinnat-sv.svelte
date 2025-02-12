<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import { announceAssertively } from '@/utilities/announce';
  import H2 from '@Component/H2';

  const title = 'Utlämnande av uppgifter och gränssnitt';
  announceAssertively(title);

  let component;
  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
</script>

<style>
  ol {
    list-style: decimal;
    @apply my-4;
  }

  ul {
    list-style: disc;
    @apply my-4;
  }

  p {
    @apply mb-4;
  }
</style>

<Container {...containerStyles.beige}>
  <div bind:this={component}>
    <InfoBlock {title} showIcon={false}>
      <p>
        Organisationerna kan få uppgifter om energicertifikat på tre olika sätt
        från det energicertifikatregister som Centralen för statligt stött
        bostadsbyggande (Varke) upprätthåller.
      </p>
      <ol class="ml-6">
        <li>
          <Link href="/energiatodistusrekisterin-aineistopalvelu"
            >Energicertifikatregistrets materialtjänst</Link>
          erbjuder ett gränssnitt för omfattande dataöverföring av energicertifikatmaterial.
        </li>
        <li>
          <Link href="/energiatodistusrekisterin-rajapintapalvelu"
            >Energicertifikatregistrets gränssnittstjänst
          </Link>
          gör det möjligt att söka uppgifter om enskilda energicertifikat med hjälp
          av Suomi.fi-informationsleden.
        </li>
        <li>
          Vi behandlar begäran om information som genomförs utan gränssnitt från
          fall till fall och debiterar för dem enligt vår timprislista.
          <br />
          Mer information: <Link href="mailto:energiatodistus.varke@gov.fi"
            >energiatodistus.varke@gov.fi</Link>
        </li>
      </ol>
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <H2>Anslutning till gränssnitt och dataskydd</H2>
    <p>
      Det är avgiftsbelagt att ansluta sig till tjänsterna och användningen av
      dem förutsätter ett datatillstånd beviljat av Varke. Dessutom ingår Varke
      och användaren ett separat avtal om användningen av tjänsten.
    </p>
    <p>
      Adressuppgifter och permanenta byggnadsbeteckningar för energicertifikat
      för små bostadshus betraktas som personuppgifter, så den som behandlar
      uppgifterna ska ha rätt att behandla personuppgifter. Som personuppgifter
      betraktas också uppgifter om den som upprättar energicertifikatet. Man kan
      också få tillgång till energicertifikatmaterial så att personuppgifterna
      har raderats.
    </p>
    <p>
      Varke är noggrann inom all sin verksamhet och säkerställer att dataskydd
      och informationssäkerhet uppfylls. Behandling av personuppgifter följer i
      huvudregel Varkes lagstadgade skyldigheter. <Link
        href="https://www.varke.fi/sv/om-oss/dataskydd-och-behandling-av-uppgifter">
        Läs mer om behandlingen av personuppgifter på (varke.fi)</Link
      >. Vi för ett register över de materialkunder som anslutits till
      gränssnittet. Dataskyddsbeskrivningarna finns på sidan
      <Link href="/tietoa-sivustosta">Information om webbplatsen</Link>.
    </p>
  </div>
</Container>
