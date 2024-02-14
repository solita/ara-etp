<script>
  import { onMount } from 'svelte';
  import ButtonLink, {
    styles as buttonLinkStyles
  } from '@Component/buttonlink';
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import SopimusTietopalvelunKaytosta from '@Asset/sopimus-tietopalvelun-kaytosta-luonnos-sv.pdf';
  import TietolupaHakemuslomake from '@Asset/tietolupa-hakemuslomake-sv.pdf';
  import { announceAssertively } from '@/utilities/announce';

  const title = 'Energicertifikatregistrets materialtjänst';
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
        Energicertifikatregistrets materialtjänst är avsedd för överföring av
        energicertifikatuppgifter från registret via gränssnittet till
        Materialtjänstens användare.
      </p>
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <p>
      Energicertifikatregistrets materialtjänst innehåller tre olika
      informationsmaterial:
    </p>
    <ol class="ml-6">
      <li>begränsat material som innehåller personuppgifter</li>
      <li>omfattande material som innehåller personuppgifter</li>
      <li>omfattande material som inte innehåller personuppgifter</li>
    </ol>
    <h2>MATERIALBESKRIVNINGAR</h2>
    <p>
      Informationsmaterialet beskrivs kort nedan. Närmare fältspecifika
      materialbeskrivningar finns
      <Link href="https://github.com/solita/ara-etp/wiki/Aineistopalvelu"
        >i GitHub</Link
      >.
    </p>

    <h3>Begränsat material som innehåller personuppgifter</h3>
    <ul class="ml-6">
      <li>
        uppgifter om gällande energicertifikat i energicertifikatregistret från
        och med maj 2015.
      </li>
      <li>
        uppgifter som specificerar energicertifikatet, såsom adress och
        permanent byggnadsbeteckning
      </li>
      <li>snävt resultat från beräkningen av energicertifikat</li>
      <li>uppgifter även om byggnader med en och två bostäder</li>
    </ul>
    <p>
      Lämpar sig till exempel för aktörer som beviljar lån och säkerheter och
      som har rätt att behandla personuppgifter.
    </p>
    <h3>Omfattande material som innehåller personuppgifter</h3>
    <ul class="ml-6">
      <li>
        uppgifter om energicertifikat i energicertifikatregistret från och med
        maj 2015.
      </li>
      <li>
        uppgifter som specificerar energicertifikatet, såsom adress och
        permanent byggnadsbeteckning
      </li>
      <li>
        en stor del av de byggnadsuppgifter och de beräkningsresultat som
        används vid energicertifikatberäkningen.
      </li>
      <li>uppgifter även om byggnader med en och två bostäder</li>
    </ul>
    <p>
      Lämpar sig till exempel för forskningsändamål för aktörer som har rätt att
      behandla personuppgifter.
    </p>
    <h3>Omfattande material som inte innehåller personuppgifter</h3>
    <ul class="ml-6">
      <li>
        uppgifter om energicertifikat i energicertifikatregistret från och med
        maj 2015.
      </li>
      <li>
        en stor del av de byggnadsuppgifter och de beräkningsresultat som
        används vid energicertifikatberäkningen.
      </li>
      <li>inga personuppgifter</li>
    </ul>
    <p>
      Uppgifter om upprättaren, certifikatbeteckningen, uppgifter som
      specificerar byggnaden (namn, gatuadress, permanent byggnadsbeteckning)
      har raderats ur materialet. Om det i registret finns färre än 4 byggnader
      inom postnummerområdet i fråga har postnummer och postanstalt raderats
      från adressuppgifterna.
    </p>
    <p>Lämpar sig till exempel för forskningsändamål.</p>
    <h2>ANVÄNDNINGSVILLKOR</h2>
    <p>
      Användningen av datatjänsten förutsätter att ARA har beviljat användaren
      datatillstånd. Dessutom ingår Ara och användaren ett separat avtal om
      användningen av Materialtjänsten.
    </p>
    <p>
      För anslutning till Materialtjänsten tar vi ut en anslutningsavgift enligt
      vår prislista. För de år som följer på anslutningsåret debiterar vi en
      årsavgift för användningen av tjänsten. Årsavgiften faktureras alltid i
      början av kalenderåret.
    </p>
    <p>
      Materialtjänsten kan också öppnas för en gång, t.ex. för
      forskningsändamål, varvid vi tar ut en engångsavgift enligt vår prislista.
    </p>
    <p>
      Begäran om information som genomförs utan gränssnitt behandlar vi från
      fall till fall och debiterar för dem enligt vår timprislista.
    </p>
    <p>
      Vår prislista finns på
      <Link href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)"
        >Aras webbplats</Link
      >.
    </p>
    <h2>ANSLUTNING TILL MATERIALTJÄNSTEN</h2>
    <ol class="ml-6">
      <li>
        Fyll i
        <Link href={TietolupaHakemuslomake} target="_blank">änsökan (pdf)</Link>
        och bekanta dig med
        <Link href={SopimusTietopalvelunKaytosta} target="_blank"
          >avtalsutkastet (pdf)
        </Link>.
      </li>
      <li>
        Skicka ansökan per e-post till Aras registratorskontor (<Link
          href="mailto:kirjaamo.ara@ara.fi">kirjaamo.ara@ara.fi</Link
        >). Om ansökan innehåller sekretessbelagd information ska du skicka
        ansökan som <Link
          href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)"
          >säker e-post</Link
        >.
      </li>
      <li>
        Vänta på beslutet om datatillstånd som vi skickar till den kontaktperson
        som angetts i ansökan och till sökandens officiella företrädare per
        e-post. Om beslutet om datatillstånd är positivt, skickas också avtalet
        för Materialtjänsten till sökandens officiella företrädare för
        undertecknande.
      </li>
      <li>
        Efter att avtalet undertecknats skickar vi en gränssnittsnyckel
        (API-key) med säker e-post till den tekniska kontaktperson som angetts i
        ansökan.
      </li>
      <li>
        Materialtjänsten är tillgänglig för dig.<Link
          href="https://github.com/solita/ara-etp/wiki/Aineistopalvelu"
          >Se närmare anvisningar om anslutning till gränssnittet i Github.</Link>
      </li>
    </ol>
    <h2>
      <ButtonLink
        href={SopimusTietopalvelunKaytosta}
        target="_blank"
        {...buttonLinkStyles.green}>ANSÖKAN</ButtonLink>
    </h2>
    <h2>ÅTGÄRDER VID STÖRNINGAR</h2>
    <p>
      Vid störningar i materialtjänsten ska du kontakta oss per e-post på
      adressen
      <Link href="mailto:energiatodistus@ara.fi">energiatodistus@ara.fi</Link>.
    </p>
  </div>
</Container>
