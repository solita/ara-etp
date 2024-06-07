<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  let component;
  announceAssertively($_('TIETOSUOJASELOSTE_LAATIJAREKISTERI'));

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    list-style: disc;
    @apply my-4 ml-6;
  }

  ol {
    @apply my-4 ml-6;
  }

  .alphabet-list {
    list-style: upper-latin;
  }

  .numbered-list {
    list-style: decimal;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_(
    'TIETOSUOJASELOSTE_LAATIJAREKISTERI'
  )}"
  descriptionFi={$locale === 'fi'
    ? $_('TIETOSUOJASELOSTE_LAATIJAREKISTERI')
    : undefined}
  descriptionSv={$locale === 'sv'
    ? $_('TIETOSUOJASELOSTE_LAATIJAREKISTERI')
    : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <H2>{$_('TIETOSUOJASELOSTE_LAATIJAREKISTERI')}</H2>

      <H3>Henkilötietojen käyttötarkoitus ja käsittelyperuste</H3>

      <p>
        Rekisterissä käsitellään henkilötietoja rakennuksen energiatodistuksesta
        annetun lain (50/2013) 18 §:n ja rakennusten
        energiatodistustietojärjestelmästä annetun lain (147/2015) 2 §:n
        nojalla.
      </p>

      <p>
        Aran lakisääteisenä tehtävänä on valvoa energiatodistuksia ja niiden
        laatijoita. Rekisterissä käsitellään henkilötietoja, jotka tarvitaan
        energiatodistusten laatijarekisterin ylläpitämiseksi. Osaa rekisterin
        tiedoista pidetään esillä julkisessa tietopalvelussa. Henkilötietoja
        käsitellään myös laskutusta varten.
      </p>

      <H3>Mitä henkilötietoja käsitellään?</H3>

      <p>
        Käsittelemme Energiatodistusten laatijarekisterissä energiatodistusten
        laatijoiden tietoja:
      </p>

      <ul>
        <li>nimi</li>
        <li>henkilötunnus</li>
        <li>puhelinnumero</li>
        <li>sähköpostiosoite</li>
        <li>osoite</li>
        <li>maa</li>
        <li>pätevyystaso</li>
        <li>toiminta-alue</li>
        <li>laatijapätevyyden voimassaoloaika</li>
      </ul>

      <p>Lisäksi käsittelemme yritysten tietoja:</p>

      <ul>
        <li>nimi</li>
        <li>Y-tunnus</li>
        <li>osoite</li>
        <li>maa</li>
        <li>verkkolaskuosoite</li>
        <li>verkkolaskuoperaattori</li>
      </ul>

      <p>
        Rekisteri sisältää tiedot myös laatijan laatimista energiatodistuksista.
      </p>

      <p>
        Energiatodistusten laatijoiden henkilötiedot saadaan lähtökohtaisesti
        pätevyydentoteajilta (FISE Oy ja Kiinteistöalan koulutuskeskus Kiinko).
        Jos energiatodistuksen laatija käyttää sähköistä allekirjoitusta,
        energiatodistuksen laatijan lokitiedot (henkilötunnus ja
        allekirjoitustoimintojen kellonajat) saadaan Suomi.fi-tunnistautumisen
        kautta. Yritysten tiedot ja tiedot laatijan laatimista
        energiatodistuksista saadaan energiatodistusten laatijoilta.
      </p>

      <H3>Henkilötietojen käsittely, siirto ja luovuttaminen</H3>

      <ol class="alphabet-list">
        <li>
          Manuaalinen aineisto<br />
          Manuaalinen aineisto käsitellään koulutetun henkilökunnan toimesta tietojen
          suojaustasoa vastaavassa lukituissa tiloissa.
        </li>
        <li>
          Digitaalisesti käsiteltävät aineistot<br />
          Rekisterin tiedot on suojattu asiattomalta katselulta, muuttamiselta ja
          hävittämiseltä. Suojaus perustuu käyttövaltuushallintaan, tietokantojen
          ja palvelinten tekniseen suojaukseen, tilojen fyysiseen suojaukseen, kulunvalvontaan,
          tietoliikenteen suojaukseen sekä tietojen varmuuskopiointiin. Pääsy- ja
          käsittelyoikeus tietoihin myönnetään työtehtävien perusteella.
        </li>
      </ol>

      <p>
        Pääsy järjestelmään perustuu henkilökohtaisiin käyttäjätunnuksiin.
        Laitetilat ja tiedot sijaitsevat fyysisesti EU:n alueella.
      </p>

      <p>
        Rekisterin sisältämiä tietoja ei käytetä profilointiin eikä tietoihin
        kohdisteta automaattista päätöksentekoa.
      </p>

      <p>
        Tietoja voidaan luovuttaa ainoastaan voimassa olevan lainsäädännön
        velvoittamissa ja sallimissa rajoissa tai rekisteröidyn suostumuksella.
      </p>

      <p>
        Aran verkkosivujen kautta on julkisesti haettavissa kaikkien
        rekisteröityneiden nimi, toiminta-alue ja laatijapätevyyden voimassaolo.
        Rekisteröidyt voivat itse määritellä myös osoitteen, puhelinnumeron,
        sähköpostiosoitteen ja verkkosivun julkisesti saataville.
      </p>

      <p>
        Osana tunnistautumista henkilötietoja luovutetaan suomi.fi-palveluun
        henkilöllisyyden varmistamiseksi. Laskutusta varten tietoja luovutetaan
        Valtion yhteiseen talous- ja henkilöstöhallinnon järjestelmään Kiekuun.
      </p>

      <p>
        Ara on ulkoistanut laatijarekisterin teknisen ylläpidon ja kehittämisen
        IT-palveluntarjoajalle, jonka kanssa on laadittu tietosuoja-asetuksen
        mukainen sopimus henkilötietojen käsittelystä.
      </p>

      <p>
        Tietoja ei siirretä EU:n tai ETA:n ulkopuolelle eikä kansainvälisille
        järjestöille.
      </p>

      <H3>Tietojen säilytysaika</H3>

      <p>
        Energiatodistusten laatijarekisterin sisältämien henkilötietojen
        säilytysajat pohjautuvat Aran tiedonohjaussuunnitelmaan.
      </p>

      <H3>Rekisteröidyn oikeudet</H3>

      <p>
        Rekisteröidyllä on oikeus saada rekisterinpitäjältä vahvistus siitä,
        että häntä koskevia henkilötietoja käsitellään tai että niitä ei
        käsitellä, ja jos näitä henkilötietoja käsitellään, oikeus saada pääsy
        henkilötietoihin. Rekisteröidyllä on lisäksi oikeus tehdä valitus
        valvontaviranomaiselle.
      </p>

      <p>
        Rekisteröidyllä itsellään on pääsy henkilötietoihin verkkopalvelun
        kautta. Jos rekisteröidyn käyttämästä tarkastusoikeudesta on kulunut
        vähemmän kuin yksi vuosi, voi rekisterinpitäjä periä tietojen
        antamisesta aiheutuvat hallinnollisiin kustannuksiin perustuvan maksun.
      </p>

      <p>
        Rekisteröity voi päivittää omia tietojaan verkkopalvelun kautta.
        Rekisteröidyllä on oikeus vaatia, että rekisterinpitäjä oikaisee ilman
        aiheetonta viivytystä rekisteröityä koskevat epätarkat ja virheelliset
        henkilötiedot. Jos pääsyä verkkopalveluun ei ole, rekisteröity voi tehdä
        oikaisupyynnön rekisterinpitäjän edustajalle.
      </p>

      <p>
        Energiatodistuksen laatijan tiedot poistetaan
        energiatodistustietojärjestelmälain 7 §:n mukaan rekisteristä, kun:
      </p>

      <ol class="numbered-list">
        <li>
          laatija ilmoittaa lopettavansa toimintansa tai pyytää muutoin
          tietojensa poistamista rekisteristä pysyvästi tai määräaikaisesti;
        </li>
        <li>
          laatijapätevyyden voimassaoloaika päättyy eikä pätevyyden toteaja ole
          ilmoittanut pätevyyden uusimisesta Aralle;
        </li>
        <li>laatijalle on annettu todistusten laatimiskielto;</li>
        <li>
          laatijan osalta toiminnan harjoittamisen yleiset edellytykset eivät
          enää täyty;
        </li>
        <li>Ara on saanut tiedon laatijan kuolemasta.</li>
      </ol>

      <p>
        Rekisteröidyn toimintaa voidaan kuitenkin valvoa myös sen jälkeen, kun
        hänen tietonsa on poistettu laatijarekisteristä. Rekisteröidyn tiedot
        tallennetaan tällöin valvontatietorekisteriin.
      </p>

      <p>
        Rekisteröidyn tietoja käsitellään lain nojalla, joten rekisteröidyllä ei
        ole oikeutta vastustaa henkilötietojensa käsittelyä, tai oikeutta saada
        tietojaan siirretyksi toiseen järjestelmään. Henkilötietojen käsittelyä
        ei myöskään voi pyytää rajoittamaan.
      </p>

      <p>
        Rekisteröidyllä on oikeus tehdä valitus tietosuojavaltuutetulle, jos hän
        katsoo, että henkilötietojen käsittelyssä rikotaan EU:n yleistä
        tietosuoja-asetusta.
      </p>

      <p>Tietosuojavaltuutetun toimiston yhteystiedot:</p>

      <ul class="contact-info">
        <li>PL 800, 00531 Helsinki</li>
        <li>Sähköposti: tietosuoja(at)om.fi</li>
        <li>Puhelinvaihde: 029 566 6700</li>
        <li>Kirjaamo: 029 566 6768</li>
      </ul>

      <H3>Rekisterinpitäjä</H3>

      <ul class="contact-info">
        <li>Asumisen rahoitus- ja kehittämiskeskus (Ara)</li>
        <li>PL 30, 15141 Lahti</li>
        <li>Puhelin: 029 525 0800 (vaihde)</li>
        <li>Sähköposti: kirjaamo.ara(at)ara.fi</li>
      </ul>

      <H3>Yhteyshenkilö</H3>

      <ul class="contact-info">
        <li>Kirsi Unhonen, energia-asiantuntija</li>
        <li>Asumisen rahoitus- ja kehittämiskeskus</li>
        <li>etunimi.sukunimi(at)ara.fi</li>
      </ul>
    </div>
  </div>
</Container>
