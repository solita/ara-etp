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
      Energiatodistusrekisterin Aineistopalvelu on tarkoitettu
      energiatodistustietojen siirtoon rajapinnan kautta rekisteristä
      Aineistopalvelun käyttäjälle.
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <p>
      Energiatodistusrekisterin Aineistopalvelu käsittää kolme eri
      tietoaineistoa:
    </p>
    <ul class="ml-6">
      <li>suppea henkilötietoja sisältävä aineisto</li>
      <li>laaja henkilötietoja sisältävä aineisto</li>
      <li>laaja ei henkilötietoja sisältävä aineisto</li>
    </ul>

    <h2>Aineistokuvaukset</h2>

    <p>
      Alla lyhyesti tietoaineistojen kuvaukset. Tarkemmat kenttäkohtaiset
      aineistokuvaukset löytyvät
      <Link href="https://github.com/solita/etp-core/wiki/Aineistopalvelu"
        >GitHubista</Link
      >.
    </p>

    <h3>Suppea henkilötietoja sisältävä aineisto</h3>
    <p>
      Sisältää voimassa olevien energiatodistusten tietoja
      energiatodistusrekisteristä toukokuusta 2015 lähtien. Tiedot käsittävät
      energiatodistuksen yksilöiviä tietoja, sekä energiatodistuslaskennasta
      saatavia tuloksia suppeasti. Sisältää myös tiedot 1- ja 2- asunnon
      rakennuksista.
    </p>
    <p>
      Soveltuu esimerkiksi lainoja ja vakuuksia antavien tahojen käyttöön,
      joilla on oikeus käsitellä henkilötietoja.
    </p>

    <h3>Laaja, henkilötietoja sisältävä aineisto</h3>
    <p>
      Sisältää energiatodistusten tietoja energiatodistusrekisteristä
      toukokuusta 2015 lähtien. Tiedot käsittävät laajasti
      energiatodistuslaskennassa käytettäviä rakennuksen tietoja ja
      laskentatuloksia. Sisältää myös tiedot 1- ja 2- asunnon rakennuksista.
    </p>

    <p>
      Soveltuu esimerkiksi tutkimuskäyttöön tahoille, joilla on oikeus käsitellä
      henkilötietoja.
    </p>

    <h3>Laaja, ei henkilötietoja sisältävä aineisto</h3>

    <p>
      Sisältää energiatodistusten tietoja energiatodistusrekisteristä
      toukokuusta 2015 lähtien. Tiedot käsittävät laajasti
      energiatodistuslaskennassa käytettäviä rakennuksen tietoja ja
      laskentatuloksia.
    </p>
    <p>
      Tiedot eivät sisällä henkilötietoja, koska laatijan tiedot,
      todistustunnus, rakennuksen yksilöivät tiedot (nimi, katuosoite, pysyvä
      rakennustunnus) ovat poistettu. Osoitetiedoista postinumero ja
      -toimipaikka on poistettu, jos kyseisellä postinumeroalueella löytyy
      vähemmän kuin 4 rakennusta rekisteristä.
    </p>
    <p>Soveltuu esimerkiksi tutkimuskäyttöön.</p>

    <h2>Käyttöehdot</h2>
    <p>
      Aineistopalvelun käyttäminen edellyttää, että ARA on myöntänyt käyttäjälle
      tietoluvan. Lisäksi ARA ja käyttäjä solmivat erillisen sopimuksen
      Aineistopalvelun käytöstä.
    </p>
    <p>
      Aineistopalveluun liittymisestä peritään liittymismaksu sekä vuosimaksu
      hinnastomme mukaisesti. Vuosimaksua ei makseta liittymisvuodelta, sillä
      liittymismaksu kattaa kuluvan vuoden vuosimaksun. Vuosimaksun laskutus
      tapahtuu aina kalenterivuoden alussa.
    </p>
    <p>
      Aineistopalvelu voidaan myös avata kertaluonteisesti esim.
      tutkimuskäyttöön, jolloin perimme kertamaksun hinnastomme mukaisesti.
    </p>
    <p>
      Ilman rajapintaa toteutettavat tietopyynnöt käsittelemme tapauskohtaisesti
      ja veloitamme niistä tuntihinnastomme mukaisesti.
    </p>
    <p>
      Hinnastomme löydät <Link
        href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)">
        ARAn sivuilta</Link
      >.
    </p>

    <h2>Aineistopalveluun liittyminen</h2>
    <ol class="ml-6">
      <li>
        Täytä <Link href={TietolupaHakemuslomake} target="_blank">hakemus</Link>
        sekä tutustu
        <Link href={SopimusTietopalvelunKaytosta} target="_blank"
          >sopimusluonnokseen</Link
        >.
      </li>
      <li>
        Lähetä hakemus sähköpostitse (kirjaamo.ara@ara.fi), jos hakemus sisältää
        salassa pidettävää tietoa, niin lähetä turvaposti (ohje löytyy <Link
          href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)"
          >ARAn sivuilta</Link
        >)
      </li>
      <li>
        Odota tietolupapäätöstä, joka toimitetaan hakemuksessa ilmoitetulle
        yhteyshenkilölle ja hakijan viralliselle edustajalle sähköpostitse. Jos
        tietolupapäätös on myönteinen, hakijan viralliselle edustajalle
        toimitetaan myös Aineistopalvelua koskeva sopimus allekirjoitettavaksi.
      </li>
      <li>
        Sopimuksen allekirjoittamisen jälkeen toimitamme API-keyn
        (rajapinta-avain) turvapostilla hakemuksessa ilmoitetulle tekniselle
        yhteyshenkilölle.
      </li>
      <li>
        Aineistopalvelu on käytettävissäsi. Ks. tarkemmat ohjeet rajapintaan
        liittymisestä <Link
          href="https://github.com/solita/etp-core/wiki/Aineistopalvelu">
          GitHubissa</Link
        >.
      </li>
    </ol>
    <ButtonLink
      href={TietolupaHakemuslomake}
      target="_blank"
      {...buttonLinkStyles.green}
      >Hakemus
    </ButtonLink>
    <h2>Toiminta häiriötilanteissa</h2>
    <p>
      Aineistopalveluun liittyvissä häiriötilanteissa ota yhteyttä sähköpostitse
      <Link href="mailto:energiatodistus@ara.fi">energiatodistus@ara.fi</Link>.
    </p>
  </div>
</Container>
