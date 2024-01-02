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
      Energicertifikatregistrets materialtjänst är avsedd för överföring av
      energicertifikatuppgifter från registret via gränssnittet till
      materialtjänstens användare.
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <p>
      Energicertifikatregistrets materialtjänst omfattar tre olika
      informationsmaterial:
    </p>
    <ul class="ml-6">
      <li>begränsat material som innehåller personuppgifter</li>
      <li>omfattande material som innehåller personuppgifter</li>
      <li>omfattande material som inte innehåller personuppgifter</li>
    </ul>

    <h2>Materialbeskrivningar</h2>

    <p>
      Nedan beskrivs informationsmaterialet kortfattat. Närmare fältspecifika
      materialbeskrivningar finns i <Link
        href="https://github.com/solita/etp-core/wiki/Aineistopalvelu">
        GitHub</Link
      >.
    </p>

    <h3>Begränsat material som innehåller personuppgifter</h3>
    <p>
      Innehåller uppgifter om gällande energicertifikat i
      energicertifikatregistret från och med maj 2015. Uppgifterna omfattar
      specifik information i energicertifikatet samt begränsade resultat av
      energicertifikatberäkningen. Innehåller även uppgifter om byggnader med
      1–2 lägenheter.
    </p>
    <p>
      Lämpar sig till exempel för aktörer som beviljar lån eller säkerheter och
      som har rätt att behandla personuppgifter.
    </p>

    <h3>Omfattande material som innehåller personuppgifter</h3>
    <p>
      Innehåller uppgifter om energicertifikat i energicertifikatregistret från
      och med maj 2015. Uppgifterna omfattar en stor del av de byggnadsuppgifter
      och de beräkningsresultat som används vid energicertifikatberäkningen.
      Innehåller även uppgifter om byggnader med 1–2 lägenheter.
    </p>

    <p>
      Lämpar sig till exempel för forskningsändamål för aktörer som har rätt att
      behandla personuppgifter.
    </p>

    <h3>Omfattande material som inte innehåller personuppgifter</h3>

    <p>
      Innehåller uppgifter om energicertifikat i energicertifikatregistret från
      och med maj 2015. Uppgifterna omfattar en stor del av de byggnadsuppgifter
      och de beräkningsresultat som används vid energicertifikatberäkningen.
    </p>
    <p>
      Uppgifterna innehåller inte personuppgifter, eftersom upprättarens
      uppgifter, certifikatbeteckningen, byggnadens specifika uppgifter (namn,
      gatuadress, permanent byggnadsbeteckning) har raderats. Om det finns färre
      än 4 byggnader i registret inom postnummerområdet i fråga har postnummer
      och postanstalt raderats från adressuppgifterna.
    </p>
    <p>Lämpar sig till exempel för forskningsändamål.</p>

    <h2>Villkor</h2>
    <p>
      Användning av datatjänsten kräver att ARA har beviljat användaren ett
      datatillstånd. Dessutom ingår ARA och användaren ett separat avtal för
      användningen av Materialtjänsten.
    </p>
    <p>
      En anslutningsavgift samt en årsavgift enligt vår prislista uppbärs för
      anslutning till materialtjänsten. Årsavgiften betalas inte för
      anslutningsåret, eftersom anslutningsavgiften täcker årsavgiften för
      innevarande år. Årsavgiften faktureras alltid i början av kalenderåret.
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
      Vår prislista finns på <Link
        href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)">
        ARAs webbplats</Link
      >.
    </p>

    <h2>Anslutning till materialtjänsten</h2>
    <ol class="ml-6">
      <li>
        Fyll i <Link href={TietolupaHakemuslomake} target="_blank"
          >ansökan</Link>
        och bekanta dig med
        <Link href={SopimusTietopalvelunKaytosta} target="_blank">
          avtalsutkastet</Link
        >.
      </li>
      <li>
        Skicka ansökan per e-post (kirjaamo.ara@ara.fi), om ansökan innehåller
        sekretessbelagd information ska du skicka den som säker e-post
        (anvisningen finna på <Link
          href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)">
          ARAs webbplats</Link
        >)
      </li>
      <li>
        Vänta på beslutet om datatillstånd som skickas till den kontaktperson
        som angetts i ansökan och till sökandens officiella företrädare per
        e-post. Om beslutet om datatillstånd är positivt, skickas också avtalet
        för materialtjänsten till sökandens officiella företrädare för
        undertecknande.
      </li>
      <li>
        Efter att avtalet undertecknats skickar vi en API-key
        (gränssnittsnyckel) med säker e-post till den tekniska kontaktperson som
        angetts i ansökan.
      </li>
      <li>
        Materialtjänsten är tillgänglig för dig. Se närmare anvisningar om
        anslutning till gränssnittet i <Link
          href="https://github.com/solita/etp-core/wiki/Aineistopalvelu">
          GitHub</Link
        >.
      </li>
    </ol>
    <ButtonLink
      href={TietolupaHakemuslomake}
      target="_blank"
      {...buttonLinkStyles.green}
      >Ansökan
    </ButtonLink>
    <h2>Åtgärder vid störningar</h2>
    <p>
      Vid störningar i materialtjänsten ska du kontakta oss per e-post på
      adressen
      <Link href="mailto:energiatodistus@ara.fi">energiatodistus@ara.fi</Link>.
    </p>
  </div>
</Container>
