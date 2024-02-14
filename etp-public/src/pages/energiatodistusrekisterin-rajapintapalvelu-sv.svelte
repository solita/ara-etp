<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import { announceAssertively } from '@/utilities/announce';
  import SopimusLuonnos from '@Asset/etp-rajapintapalvelu-sopimus-luonno-sv.pdf';

  const title = 'Energicertifikatregistrets gränssnittstjänst';
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

  h2 {
    @apply mb-2 mt-8;
  }

  h3 {
    @apply underline font-bold;
  }

  p {
    @apply mb-4;
  }
</style>

<Container {...containerStyles.beige}>
  <div bind:this={component}>
    <InfoBlock {title}>
      <p>
        Via energicertifikatregistrets gränssnittstjänst kan du söka uppgifter
        om ett enskilt energicertifikat i Suomi.fi-informationsleden antingen
        som en pdf-fil eller som strukturerade data. Som söknycklar används
        energicertifikatbeteckningen eller den permanenta byggnadsbeteckningen.
        Du hittar vår gränssnittstjänst i
        <Link href="https://liityntakatalogi.suomi.fi/"
          >Suomi.fi-anslutningsledens API-katalog
        </Link>
        .
      </p>
      <p>
        Suomi.fi-informationsleden är en tjänst som genomförs och tillhandahålls
        av Myndigheten för digitalisering och befolkningsdata. Det möjliggör
        dataöverföring mellan organisationer som anslutit sig till leden.
        <Link href="https://dvv.fi/palveluvayla"
          >Mer information om informationsleden (dvv.fi)
        </Link>
        .
      </p>
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <h2>ANVÄNDNINGSVILLKOR</h2>
    <p>
      Användningen av energicertifikatregistrets gränssnittstjänst förutsätter
      ett datatillstånd beviljat av Ara. Dessutom ingår Ara och tjänstens
      användare ett separat avtal. Uppgifterna om energicertifikat innehåller
      personuppgifter, så endast de användare som har rätt att behandla
      personuppgifter kan ansluta sig till tjänsten.
    </p>

    <p>
      Användningen av informationsleden förutsätter också en separat
      anslutningsserver.
    </p>

    <p>
      För anslutning till gränssnittstjänsten tar ut vi en anslutningsavgift
      enligt vår prislista. För de år som följer på anslutningsåret debiterar vi
      en årsavgift för användningen av tjänsten. Årsavgiften faktureras alltid i
      början av kalenderåret.
    </p>

    <p>
      Vår prislista finns på
      <Link href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)"
        >Aras webbplats
      </Link>
      .
    </p>

    <h2>ANSLUTNING TILL TJÄNSTEN</h2>
    <ol class="ml-6">
      <li>
        Anslut dig till informationsleden enligt
        <Link href="https://dvv.fi/palveluvayla">MDB:s anvisningar</Link>
        .
      </li>
      <li>
        Fyll i
        <Link
          href="https://liityntakatalogi.test.suomi.fi/dataset/energiatodistusrekisteri-ara-svc"
          >ansökan
        </Link> som finns i API-katalogens testversion.
      </li>
      <li>
        Väntä på beslutet om datatillstånd som vi skickar till den kontaktperson
        som angetts i ansökan och till sökandens officiella företrädare per
        e-post. Om beslutet om datatillstånd är positivt, skickas också <Link
          href={SopimusLuonnos}
          target="_blank">avtalet</Link>
        för informationsleden till sökandens officiella företrädare för undertecknande.
      </li>
      <li>
        Efter att avtalet har undertecknats öppnar vi
        informationsledsgränssnittet för anslutning till testversionen.
      </li>
      <li>
        Fyll i
        <Link
          href="https://liityntakatalogi.suomi.fi/dataset/energiatodistusrekisteri-ara-svc"
          >ansökan</Link>
        som finns i API-katalogen och vänta på Aras godkännande.
      </li>
      <li>
        Du har tillgång till energicertifikatregistrets gränssnittstjänst
        <Link href="https://github.com/solita/ara-etp/wiki/Rajapintapalvelu"
          >Se närmare anvisningar om anslutning till gränssnittet i Github</Link
        >.
      </li>
    </ol>
  </div>
</Container>
