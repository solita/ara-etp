<script>
  import { onMount } from 'svelte';
  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';

  let component;

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    @apply my-4 ml-6 list-disc;
  }

  ol {
    @apply my-4 ml-6;
  }

  .alphabet-list {
    list-style: upper-latin;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<div bind:this={component}>
  <H1 text="Tietosuojaseloste Energiatodistusten laatijarekisteri" />
  <H2 text="Henkilötietojen käyttötarkoitus ja käsittelyperuste" />

  <p>
    Rekisterissä käsitellään henkilötietoja rakennuksen energiatodistuksesta
    annetun lain (50/2013) 18 §:n ja rakennusten
    energiatodistustietojärjestelmästä annetun lain (147/2015) 2 §:n nojalla.
  </p>

  <p>
    Aran lakisääteisenä tehtävänä on valvoa energiatodistuksia ja niiden
    laatijoita. Rekisterissä käsitellään henkilötietoja, jotka tarvitaan
    energiatodistusten laatijarekisterin ylläpitämiseksi. Osaa rekisterin
    tiedoista pidetään esillä julkisessa tietopalvelussa. Henkilötietoja
    käsitellään myös laskutusta varten.
  </p>

  <H2 text="Mitä henkilötietoja käsitellään?" />

  <p>
    Käsittelemme Energiatodistusten laatijarekisterissä energiatodistusten
    laatijoiden tietoja:
  </p>

  <ul>
    <li>nimi</li>
    <li>henkilötunnus</li>
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
    Laatijoiden henkilötiedot saadaan pätevyydentoteajilta (FISE Oy ja
    Kiinteistöalan koulutuskeskus Kiinko).
  </p>

  <H2 text="Henkilötietojen käsittely, siirto ja luovuttaminen" />

  <ol class="alphabet-list">
    <li>
      Manuaalinen aineisto<br />
      Manuaalinen aineisto käsitellään koulutetun henkilökunnan toimesta tietojen
      suojaustasoa vastaavassa lukituissa tiloissa.
    </li>
    <li>
      Digitaalisesti käsiteltävät aineistot<br />
      Rekisterin tiedot on suojattu asiattomalta katselulta, muuttamiselta ja hävittämiseltä.
      Suojaus perustuu käyttövaltuushallintaan, tietokantojen ja palvelinten tekniseen
      suojaukseen, tilojen fyysiseen suojaukseen, kulunvalvontaan, tietoliikenteen
      suojaukseen sekä tietojen varmuuskopiointiin. Pääsy- ja käsittelyoikeus tietoihin
      myönnetään työtehtävien perusteella.
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

  <H2 text="Tietojen säilytysaika" />

  <p>
    Energiatodistusten laatijarekisterin sisältämien henkilötietojen
    säilytysajat pohjautuvat Aran tiedonohjaussuunnitelmaan.
  </p>

  <H2 text="Rekisteröidyn oikeudet" />

  <p>
    Rekisteröidyllä on oikeus saada rekisterinpitäjältä vahvistus siitä, että
    häntä koskevia henkilötietoja käsitellään tai että niitä ei käsitellä, ja
    jos näitä henkilötietoja käsitellään, oikeus saada pääsy henkilötietoihin.
    Rekisteröidyllä on lisäksi oikeus tehdä valitus valvontaviranomaiselle.
  </p>

  <p>
    Rekisteröidyllä itsellään on pääsy henkilötietoihin verkkopalvelun kautta.
    Jos rekisteröidyn käyttämästä tarkastusoikeudesta on kulunut vähemmän kuin
    yksi vuosi, voi rekisterinpitäjä periä tietojen antamisesta aiheutuvat
    hallinnollisiin kustannuksiin perustuvan maksun.
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
      laatija ilmoittaa lopettavansa toimintansa tai pyytää muutoin tietojensa
      poistamista rekisteristä pysyvästi tai määräaikaisesti;
    </li>
    <li>
      laatijapätevyyden voimassaoloaika päättyy eikä pätevyyden toteaja ole
      ilmoittanut pätevyyden uusimisesta Aralle;
    </li>
    <li>laatijalle on annettu todistusten laatimiskielto;</li>
    <li>
      laatijan osalta toiminnan harjoittamisen yleiset edellytykset eivät enää
      täyty;
    </li>
    <li>Ara on saanut tiedon laatijan kuolemasta.</li>
  </ol>

  <p>
    Rekisteröidyn toimintaa voidaan kuitenkin valvoa myös sen jälkeen, kun hänen
    tietonsa on poistettu laatijarekisteristä. Rekisteröidyn tiedot tallennetaan
    tällöin valvontatietorekisteriin.
  </p>

  <p>
    Rekisteröidyn tietoja käsitellään lain nojalla, joten rekisteröidyllä ei ole
    oikeutta vastustaa henkilötietojensa käsittelyä, tai oikeutta saada
    tietojaan siirretyksi toiseen järjestelmään. Henkilötietojen käsittelyä ei
    myöskään voi pyytää rajoittamaan.
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

  <H2 text="Rekisterinpitäjä" />

  <ul class="contact-info">
    <li>Asumisen rahoitus- ja kehittämiskeskus (Ara)</li>
    <li>PL 30, 15141 Lahti</li>
    <li>Puhelin: 029 525 0800 (vaihde)</li>
    <li>Sähköposti: kirjaamo.ara(at)ara.fi</li>
  </ul>

  <H2 text="Yhteyshenkilö" />

  <ul class="contact-info">
    <li>Kirsi Unhonen, energia-asiantuntija</li>
    <li>Asumisen rahoitus- ja kehittämiskeskus</li>
    <li>etunimi.sukunimi(at)ara.fi</li>
  </ul>
</div>
