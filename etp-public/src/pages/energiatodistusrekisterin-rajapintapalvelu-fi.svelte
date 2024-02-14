<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import { announceAssertively } from '@/utilities/announce';
  import SopimusLuonnos from '@Asset/etp-rajapintapalvelu-sopimus-luonnos-fi.pdf';

  const title = 'Energiatodistusrekisterin rajapintapalvelu';
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
        Energiatodistusrekisterin rajapintapalvelun kautta voit hakea
        yksittäisen energiatodistuksen tiedot Suomi.fi-palveluväylästä joko
        pdf-tiedostona tai rakenteellisena tietona. Hakuavaimina käytetään
        energiatodistustunnusta tai pysyvää rakennustunnusta. Löydät
        rajapintapalvelumme
        <Link href="https://liityntakatalogi.suomi.fi/"
          >Suomi.fi-palveluväylän liityntäkatalogista</Link
        >.
      </p>
      <p>
        Suomi.fi-palveluväylä on Digi- ja väestötietoviraston toteuttama ja
        tarjoama palvelu. Se mahdollistaa tiedonsiirron väylään liittyneiden
        organisaatioiden välillä.
        <Link href="https://dvv.fi/palveluvayla"
          >Lisätietoa palveluväylästä (dvv.fi)</Link
        >.
      </p>
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <h2>KÄYTTÖEHDOT</h2>
    <p>
      Energiatodistusrekisterin rajapintapalvelun käyttö edellyttää Aran
      myöntämää tietolupaa. Sen lisäksi Ara ja palvelun käyttäjä solmivat
      erillisen sopimuksen. Energiatodistustiedot sisältävät henkilötietoa,
      joten palveluun voivat liittyä vain ne käyttäjät, joilla on oikeus
      käsitellä henkilötietoa.
    </p>

    <p>Palveluväylän käyttö edellyttää myös erillistä liityntäpalvelinta.</p>

    <p>
      Rajapintapalveluun liittymisestä perimme hinnastomme mukaisen
      liittymismaksun. Liittymisvuotta seuraavilta vuosilta perimme palvelun
      käytöstä vuosimaksua. Vuosimaksu laskutetaan aina kalenterivuoden alussa.
    </p>

    <p>
      Hinnastomme löydät
      <Link href="https://www.ara.fi/fi-FI/ARA/Yhteystiedot/Yhteystiedot(477)"
        >Aran sivuilta</Link
      >.
    </p>

    <h2>RAJAPINTAPALVELUUN LIITTYMINEN</h2>
    <ol class="ml-6">
      <li>
        Liity palveluväylään <Link href="https://dvv.fi/palveluvayla"
          >DVV:n ohjeiden mukaisesti</Link
        >.
      </li>
      <li>
        Täytä liityntäkatalogin testiveriosta löytyvä <Link
          href="https://liityntakatalogi.test.suomi.fi/dataset/energiatodistusrekisteri-ara-svc"
          >hakemus</Link
        >.
      </li>
      <li>
        Odota tietolupapäätöstä, jonka lähetämme hakemuksessa ilmoitetulle
        yhteyshenkilölle ja hakijan viralliselle edustajalle sähköpostitse. Jos
        tietolupapäätös on myönteinen, hakijan viralliselle edustajalle
        toimitetaan myös palveluväylää koskeva <Link
          href={SopimusLuonnos}
          target="_blank">sopimus</Link> allekirjoitettavaksi.
      </li>
      <li>
        Sopimuksen allekirjoittamisen jälkeen avaamme palveluväylärajapinnan
        testiversioon liittymistä varten.
      </li>
      <li>
        Täytä liityntäkatalogista löytyvä <Link
          href="https://liityntakatalogi.suomi.fi/dataset/energiatodistusrekisteri-ara-svc"
          >hakemus</Link> ja odota Aran hyväksyntää.
      </li>
      <li>
        Energiatodistusrekisterin rajapintapalvelu on käytettävissäsi. <Link
          href="https://github.com/solita/ara-etp/wiki/Aineistopalvelu"
          >Katso tarkemmat ohjeet rajapintaan liittymisestä GitHubissa</Link
        >.
      </li>
    </ol>
  </div>
</Container>
