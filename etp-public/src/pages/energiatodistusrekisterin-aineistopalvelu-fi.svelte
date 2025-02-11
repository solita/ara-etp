<script>
  import { onMount } from 'svelte';
  import ButtonLink, {
    styles as buttonLinkStyles
  } from '@Component/buttonlink';
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import SopimusTietopalvelunKaytosta from '@Asset/sopimus-tietopalvelun-kaytosta-luonnos.pdf';
  import TietolupaHakemuslomake from '@Asset/tietolupa-hakemuslomake.pdf';
  import { announceAssertively } from '@/utilities/announce';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  const title = 'Energiatodistusrekisterin aineistopalvelu';
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
        Energiatodistusrekisterin aineistopalvelu on tarkoitettu
        energiatodistustietojen siirtoon rajapinnan kautta rekisteristä
        Aineistopalvelun käyttäjälle.
      </p>
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <p>
      Energiatodistusrekisterin aineistopalvelussa on kolme erilaista
      tietoaineistoa:
    </p>
    <ol class="ml-6">
      <li>suppea henkilötietoja sisältävä aineisto</li>
      <li>laaja henkilötietoja sisältävä aineisto</li>
      <li>laaja ei henkilötietoja sisältävä aineisto</li>
    </ol>
    <H2>AINEISTOKUVAUKSET</H2>
    <p>
      Tietoaineistot on kuvattu lyhyesti alla. Tarkemmat kenttäkohtaiset
      aineistokuvaukset löytyvät <Link
        href="https://github.com/solita/ara-etp/wiki/Aineistopalvelu"
        >GitHubista</Link
      >.
    </p>

    <H3>Suppea henkilötietoja sisältävä aineisto</H3>
    <ul class="ml-6">
      <li>
        voimassa olevien energiatodistusten tietoja energiatodistusrekisteristä
        toukokuusta 2015 lähtien
      </li>
      <li>
        energiatodistuksen yksilöiviä tietoja, kuten osoite ja pysyvä
        rakennustunnus
      </li>
      <li>suppeasti energiatodistuslaskennasta saatavia tuloksia</li>
      <li>tiedot myös yhden ja kahden asunnon rakennuksista</li>
    </ul>
    <p>
      Soveltuu esimerkiksi lainoja ja vakuuksia antavien tahojen käyttöön,
      joilla on oikeus käsitellä henkilötietoja.
    </p>
    <H3>Laaja, henkilötietoja sisältävä aineisto</H3>
    <ul class="ml-6">
      <li>
        energiatodistusten tietoja energiatodistusrekisteristä toukokuusta 2015
        lähtien
      </li>
      <li>
        energiatodistuksen yksilöiviä tietoja, kuten osoite ja pysyvä
        rakennustunnus
      </li>
      <li>
        laajasti energiatodistuslaskennassa käytettäviä rakennusten tietoja ja
        laskentatuloksia
      </li>
      <li>tiedot myös yhden ja kahden asunnon rakennuksista</li>
    </ul>
    <p>
      Soveltuu esimerkiksi tutkimuskäyttöön tahoille, joilla on oikeus käsitellä
      henkilötietoja.
    </p>
    <H3>Laaja, ei henkilötietoja sisältävä aineisto</H3>
    <ul class="ml-6">
      <li>
        energiatodistusten tietoja energiatodistusrekisteristä toukokuusta 2015
        lähtien
      </li>
      <li>
        laajasti energiatodistuslaskennassa käytettäviä rakennuksen tietoja ja
        laskentatuloksia
      </li>
      <li>ei henkilötietoja</li>
    </ul>
    <p>
      Aineistosta on poistettu laatijan tiedot, todistustunnus, rakennuksen
      yksilöivät tiedot (nimi, katuosoite, pysyvä rakennustunnus).
      Osoitetiedoista postinumero ja -toimipaikka on poistettu, jos rekisterissä
      on kyseiseltä postinumeroalueelta vähemmän kuin 4 rakennusta.
    </p>
    <p>Soveltuu esimerkiksi tutkimuskäyttöön.</p>
    <H2>KÄYTTÖEHDOT</H2>
    <p>
      Aineistopalvelun käyttäminen edellyttää, että Varke on myöntänyt käyttäjälle
      tietoluvan. Lisäksi Varke ja käyttäjä solmivat erillisen sopimuksen
      Aineistopalvelun käytöstä.
    </p>
    <p>
      Aineistopalveluun liittymisestä perimme hinnastomme mukaisen
      liittymismaksun. Liittymisvuotta seuraavilta vuosilta perimme palvelun
      käytöstä vuosimaksua. Vuosimaksu laskutetaan aina kalenterivuoden alussa.
    </p>
    <p>
      Aineistopalvelu voidaan myös avata kertaluonteisesti esimerkiksi
      tutkimuskäyttöön, jolloin perimme kertamaksun hinnastomme mukaisesti.
    </p>
    <p>
      Ilman rajapintaa toteutettavat tietopyynnöt käsittelemme tapauskohtaisesti
      ja veloitamme niistä tuntihinnastomme mukaisesti.
    </p>
    <p>
      Hinnastomme löydät
      <Link
        href="https://www.ara.fi/fi/tietoa-meista/yhteystiedot/maksullisten-palvelujen-hinnat"
        >Varken sivuilta</Link>
      .
    </p>
    <H2>AINEISTOPALVELUUN LIITTYMINEN</H2>
    <ol class="ml-6">
      <li>
        Täytä
        <Link href={TietolupaHakemuslomake} target="_blank">hakemus (pdf)</Link>
        sekä tutustu
        <Link href={SopimusTietopalvelunKaytosta} target="_blank"
          >sopimusluonnokseen (pdf)</Link
        >.
      </li>
      <li>
        Lähetä hakemus sähköpostitse Varken kirjaamoon (<Link
          href="mailto:varke.ym@gov.fi">varke.ym@gov.fi</Link
        >). Jos hakemus sisältää salassa pidettävää tietoa, lähetä hakemus <Link
          href="https://turvaviesti.ara.fi/">turvapostilla</Link
        >.
      </li>
      <li>
        Odota tietolupapäätöstä, jonka lähetämme hakemuksessa ilmoitetulle
        yhteyshenkilölle ja hakijan viralliselle edustajalle sähköpostitse. Jos
        tietolupapäätös on myönteinen, toimitamme hakijan viralliselle
        edustajalle myös Aineistopalvelua koskevan sopimuksen
        allekirjoitettavaksi.
      </li>
      <li>
        Sopimuksen allekirjoittamisen jälkeen toimitamme rajapinta-avaimen
        (API-key) turvapostilla hakemuksessa ilmoitetulle tekniselle
        yhteyshenkilölle.
      </li>
      <li>
        Aineistopalvelu on käytettävissäsi. <Link
          href="https://github.com/solita/ara-etp/wiki/Aineistopalvelu"
          >Katso tarkemmat ohjeet rajapintaan liittymisestä GitHubissa.</Link>
      </li>
    </ol>
    <ButtonLink
      href={TietolupaHakemuslomake}
      target="_blank"
      {...buttonLinkStyles.green}>HAKEMUS</ButtonLink>
    <H2>TOIMINTA HÄIRIÖTILANTEISSA</H2>
    <p>
      Aineistopalveluun liittyvissä häiriötilanteissa ota yhteyttä sähköpostitse
      <Link href="mailto:energiatodistus.varke@gov.fi">energiatodistus.varke@gov.fi</Link>.
    </p>
  </div>
</Container>
