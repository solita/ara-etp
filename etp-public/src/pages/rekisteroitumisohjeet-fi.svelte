<script>
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import H2 from '@Component/H2';
</script>

<style>
  ul {
    list-style: disc;
    @apply my-4 ml-6;
  }

  ol {
    @apply my-4 ml-6;
  }

  .numbered-list {
    list-style: decimal;
  }

  p {
    @apply mb-4;
  }
</style>

<Container {...containerStyles.beige}>
  <InfoBlock title="Ohjeet energiatodistusten laatijoiden palvelun käyttöön">
    Vain laatijarekisteriin rekisteröity käyttäjä voi kirjautua palveluun ja
    laatia energiatodistuksia.
  </InfoBlock>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <div class="border-b border-grey pb-8 mb-8">
      <H2>Kuinka minut lisätään laatijarekisteriin?</H2>
      <p>
        Pätevyyskokeen hyväksytysti suorittaneet laatijat lisätään
        laatijarekisteriin kokeiden arvioinnin jälkeen. Rekisteriin lisäyksen
        tekevät pätevyyden toteajat (FISE tai Kiinko).
      </p>
    </div>
    <div class="border-b border-grey pb-8 mb-8">
      <H2>Kuinka pääsen käyttämään palvelua?</H2>
      <ol class="numbered-list">
        <li>
          <p>Kirjaudu Suomi.fi-tunnuksilla sisään.</p>
        </li>
        <li>
          <p>Tarkasta, että omat tietosi ovat palvelussa oikein.</p>
        </li>
      </ol>

      <p class="my-4">
        Jos kirjautuminen ei onnistu, on mahdollista, että pätevyyden toteaja ei
        ole vielä rekisteröinyt sinua laatijarekisteriin. Odota, että saat
        pätevyyden toteajalta laskun. Laskun eräpäivän jälkeen pätevyyden
        toteaja lisää sinut laatijarekisteriin, jos olet suorittanut maksun.
      </p>
    </div>
    <div class="mb-8">
      <H2>ENERGIATODISTUKSEN ALLEKIRJOITUS</H2>
      <p>
        Energiatodistus allekirjoitetaan sähköisesti. Todistuksen
        allekirjoitukseen on kaksi tapaa:
      </p>
      <ol class="numbered-list">
        <li>
          <p>
            <b>Suomi.fi-tunnistautumiseen</b> perustuva allekirjoitus, jossa käytetään
            järjestelmän allekirjoitusvarmennetta. Tunnistautuminen tapahtuu kirjautuessasi
            sisään rekisteriin. Kun allekirjoitat todistuksen sinun ei tarvitse enää
            tunnistautua uudelleen. Tunnistautuminen vaaditaan allekirjoitettaessa
            vain, jos rekisteriin kirjautumisesta on kulunut yli 1,5 tuntia.
          </p>
        </li>
        <li>
          <p>
            <b>Sähköisellä henkilökortilla</b> eli varmennekortilla tapahtuva allekirjoitus,
            jossa käytetään henkilökohtaista allekirjoitusvarmennetta. Tätä varten
            tarvitset henkilökortin, kortinlukulaitteen ja kortinlukuohjelmiston.
          </p>
          <ul>
            <li>
              <p>
                Korttia haetaan poliisilta:
                <Link href="https://poliisi.fi/etusivu">
                  Siirry poliisin sivuille henkilökortin hakemiseen
                </Link>
              </p>
            </li>
            <li>
              <p>Kortinlukulaitteen voi ostaa elektroniikkaliikkeistä.</p>
            </li>
            <li>
              <p>
                Kortinlukijaohjelmiston voi ladata ja asentaa Digi- ja
                väestötietovirastosta.
                <Link href="https://dvv.fi/kortinlukijaohjelmisto">
                  Siirry kortinlukijaohjelmiston sivulle (dvv.fi)
                </Link>
              </p>
            </li>
          </ul>
        </li>
      </ol>
      <p>
        Lisätietoja sähköisestä allekirjoituksesta ja henkilökortista löydät
        Digi- ja väestötietoviraston sivuilta:
        <Link href="https://dvv.fi/varmenteet">
          varmenteet ja kortit (dvv.fi)
        </Link>
      </p>
    </div>
  </div>
</Container>
