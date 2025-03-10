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
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

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

  p {
    @apply mb-4;
  }
</style>

<Container {...containerStyles.beige}>
  <div bind:this={component}>
    <InfoBlock {title} showIcon={false}>
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
    <H2>MATERIALBESKRIVNINGAR</H2>
    <p>
      Informationsmaterialet beskrivs kort nedan. Närmare fältspecifika
      materialbeskrivningar finns
      <Link href="https://github.com/solita/ara-etp/wiki/Aineistopalvelu"
        >i GitHub</Link
      >.
    </p>

    <H3>Begränsat material som innehåller personuppgifter</H3>
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
    <H3>Omfattande material som innehåller personuppgifter</H3>
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
    <H3>Omfattande material som inte innehåller personuppgifter</H3>
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
    <H2>ANVÄNDNINGSVILLKOR</H2>
    <p>
      Användningen av datatjänsten förutsätter att Varke har beviljat användaren
      datatillstånd. Dessutom ingår Varke och användaren ett separat avtal om
      användningen av Materialtjänsten.
    </p>
    <p>
      För anslutning till Materialtjänsten tar vi ut en anslutningsavgift enligt
      vår prislista. För de år som följer på anslutningsåret debiterar vi en
      årsavgift för användningen av tjänsten. Årsavgiften faktureras alltid i
      början av kalenderåret.
    </p>
    <p>
      Materialtjänsten kan också öppnas för en gång, till exempel för
      forskningsändamål, varvid vi tar ut en engångsavgift enligt vår prislista.
    </p>
    <p>
      Begäran om information som genomförs utan gränssnitt behandlar vi från
      fall till fall och debiterar för dem enligt vår timprislista.
    </p>
    <p>
      Vår prislista finns på
      <Link
        href="https://www.varke.fi/sv/om-oss/kontaktuppgifter/priser-avgiftsbelagda-tjanster"
        >Varkes webbplats</Link
      >.
    </p>
    <H2>ANSLUTNING TILL MATERIALTJÄNSTEN</H2>
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
        Skicka ansökan per e-post till Varkes registratorskontor (<Link
          href="mailto:varke.ym@gov.fi">varke.ym@gov.fi</Link
        >). Om ansökan innehåller sekretessbelagd information ska du skicka
        ansökan som <Link href="https://turvaviesti.varke.fi/"
          >säker e-post</Link
        >.
      </li>
      <li>
        Vänta på beslutet om datatillstånd som vi skickar till den kontaktperson
        som angetts i ansökan och till sökandens officiella företrädare per
        e-post. Om beslutet om datatillstånd är positivt, skickar vi också
        avtalet för Materialtjänsten till sökandens officiella företrädare för
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
    <ButtonLink
      href={SopimusTietopalvelunKaytosta}
      target="_blank"
      {...buttonLinkStyles.green}>ANSÖKAN</ButtonLink>
    <H2>ÅTGÄRDER VID STÖRNINGAR</H2>
    <p>
      Vid störningar i materialtjänsten ska du kontakta oss per e-post på
      adressen
      <Link href="mailto:energiatodistus.varke@gov.fi"
        >energiatodistus.varke@gov.fi</Link
      >.
    </p>
  </div>
</Container>
