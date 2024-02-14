<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import { announceAssertively } from '@/utilities/announce';

  const title = 'Tietojenluovutus ja rajapinnat';
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
        Organisaatiot voivat saada Asumisen rahoitus- ja kehittämiskeskuksen
        (Ara) ylläpitämästä energiatodistusrekisteristä energiatodistustietoa
        kolmella eri tavalla.
      </p>
      <ol class="ml-6">
        <li>
          <Link href="/energiatodistusrekisterin-aineistopalvelu"
            >Energiatodistusrekisterin aineistopalvelu</Link>
          tarjoaa rajapinnan energiatodistusaineistojen laajaan tiedonsiirtoon.
        </li>
        <li>
          <Link href="/energiatodistusrekisterin-rajapintapalvelu"
            >Energiatodistusrekisterin rajapintapalvelu</Link>
          mahdollistaa yksittäisten energiatodistusten tietojen haun Suomi.fi-palveluväylän
          avulla
        </li>
        <li>
          Käsittelemme ilman rajapintaa toteutettavat tietopyynnöt
          tapauskohtaisesti ja veloitamme niistä tuntihinnastomme mukaisesti. <br />
          Lisätietoja:
          <Link href="mailto:energiatodistus@ara.fi"
            >energiatodistus@ara.fi</Link>
        </li>
      </ol>
    </InfoBlock>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <h2>Rajapintoihin liittyminen ja tietosuoja</h2>
    <p>
      Palveluihin liittyminen on maksullista ja niiden käyttäminen edellyttää
      Aran myöntämää tietolupaa. Lisäksi Ara ja käyttäjä solmivat erillisen
      sopimuksen palvelun käytöstä.
    </p>
    <p>
      Pienten asuinrakennusten energiatodistusten osoitetiedot ja pysyvä
      rakennustunnus katsotaan henkilötiedoiksi, joten tietojen käsittelijällä
      tulee olla oikeus käsitellä henkilötietoja. Henkilötiedoiksi katsotaan
      myös energiatodistuksen laatijan tiedot. Energiatodistusaineistoja voi
      saada käyttöön myös niin, että henkilötiedot on poistettu.
    </p>
    <p>
      Ara noudattaa kaikessa toiminnassaan huolellisuutta ja varmistaa, että
      tietosuoja ja tietoturvallisuus toteutuvat. Henkilötietojen käsittely
      perustuu pääsääntöisesti Aran lakisääteisten velvoitteiden noudattamiseen.
      Lue lisää henkilötietojen käsittelystä
      <Link
        href="https://www.ara.fi/fi-FI/Tietosuoja_ja_henkilotietojen_kasittely_(48134)">
        sivultamme.</Link>

      Pidämme rekisteriä rajapintaan liitetyistä aineistoasiakkaista.
      Tietosuojaselosteet ovat sivulla
      <Link href="/tietoa-sivustosta">Tietoa sivustosta</Link>.
    </p>
  </div>
</Container>
