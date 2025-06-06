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
  <H1 text="Tietosuojaseloste Energiatodistusrekisteri" />

  <H2 text="Henkilötietojen käyttötarkoitus ja käsittelyperuste" />

  <p>
    Rekisterissä käsitellään henkilötietoja rakennuksen energiatodistuksesta
    annetun lain (50/2013) 18 §:n ja rakennusten
    energiatodistustietojärjestelmästä annetun lain (147/2015) 3 §:n nojalla.
  </p>

  <p>
    Varken lakisääteisenä tehtävänä on valvoa energiatodistuksia ja niiden
    laatijoita. Rekisterissä käsitellään henkilötietoja, jotka tarvitaan
    energiatodistusrekisterin ylläpitämiseksi.
  </p>

  <H2 text="Mitä henkilötietoja käsitellään?" />

  <p>
    Käsittelemme Energiatodistusrekisterissä energiatodistusten laatijoiden
    tietoja:
  </p>

  <ul>
    <li>nimi</li>
    <li>henkilötunnus</li>
    <li>sähköpostiosoite</li>
    <li>osoite</li>
    <li>maa</li>
    <li>pätevyystaso</li>
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
    Lisäksi käsittelemme henkilötiedoiksi katsottavia rakennuksen tarkkoja
    katuosoitteita (alle 2 asunnon pientalot) ja rakennukseen liittyviä pysyviä
    rakennustunnuksia.
  </p>

  <H2 text="Henkilötietojen käsittely, siirto ja luovuttaminen" />

  <ol class="alphabet-list">
    <li>
      Manuaalinen aineisto<br />
      Manuaalista aineistoa käsittelevät tietoturvakoulutetut henkilöt. Rekisterin
      tiedot on suojattu asiattomalta katselulta, muuttamiselta ja hävittämiseltä.
    </li>
    <li>
      Digitaalisesti käsiteltävät aineistot<br />
      Rekisterin tiedot on suojattu asiattomalta katselulta, muuttamiselta ja hävittämiseltä.
      Suojaus perustuu käyttövaltuushallintaan, tietokantojen ja palvelinten tekniseen
      suojaukseen, tilojen fyysiseen suojaukseen, kulunvalvontaan, tietoliikenteen
      suojaukseen sekä tietojen varmuuskopiointiin.
    </li>
  </ol>

  <p>
    Pääsy- ja käsittelyoikeus tietoihin myönnetään työtehtävien perusteella.
    Pääsy järjestelmään perustuu henkilökohtaisiin käyttäjätunnuksiin.
    Laitetilat ja tiedot sijaitsevat fyysisesti EU:n alueella.
  </p>

  <p>
    Varke on ulkoistanut energiatodistusrekisterin teknisen ylläpidon ja
    kehittämisen IT-palveluntarjoajalle, jonka kanssa on solmittu
    tietosuoja-asetuksen mukainen sopimus henkilötietojen käsittelystä.
  </p>

  <p>
    Tietoja voidaan luovuttaa ainoastaan voimassa olevan lainsäädännön
    velvoittamissa ja sallimissa rajoissa tai rekisterinpitäjän suostumuksella.
    Laskutusta varten tietoja luovutetaan Valtion yhteiseen talous- ja
    henkilöstöhallinnon järjestelmään Kiekuun.
  </p>

  <p>
    Tietoja ei siirretä EU:n tai ETA:n ulkopuolelle eikä kansainvälisille
    järjestöille.
  </p>

  <H2 text="Tietojen säilytysaika" />

  <p>
    Energiatodistusrekisterin sisältämien henkilötietojen säilytysajat
    pohjautuvat Varken tiedonohjaussuunnitelmaan.
  </p>

  <H2 text="Rekisteröidyn oikeudet" />

  <p>
    Rekisteröidyllä on oikeus saada rekisterinpitäjältä vahvistus siitä, että
    häntä koskevia henkilötietoja käsitellään tai että niitä ei käsitellä, ja
    jos näitä henkilötietoja käsitellään, oikeus saada pääsy henkilötietoihin.
    Rekisterien sisältämiä tietoja ei käytetä profilointiin eikä tietoihin
    kohdisteta automaattista päätöksentekoa. Jos rekisteröidyn käyttämästä
    tarkastusoikeudesta on kulunut vähemmän kuin yksi vuosi, voi
    rekisterinpitäjä periä tietojen antamisesta aiheutuvat hallinnollisiin
    kustannuksiin perustuvan maksun.
  </p>

  <p>
    Sähköisesti allekirjoitettua energiatodistusta ei voi muuttaa. Jos
    energiatodistus sisältää virheellisiä henkilötietoja, siitä voi ilmoittaa
    rekisterinpitäjälle, joka asettaa todistuksen käyttökieltoon.
  </p>

  <p>
    Rekisteröidyllä ei ole oikeutta pyytää tietojensa poistamista, koska
    tietojen käsittely perustuu lakiin. Tästä syystä rekisteröidyllä ei myöskään
    ole oikeutta vastustaa henkilötietojensa käsittelyä, tai oikeutta saada
    tietojaan siirretyksi toiseen järjestelmään. Henkilötietojen käsittelyä ei
    myöskään voi pyytää rajoittamaan, koska tietoja käsitellään lain nojalla.
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
    <li>Valtion tukeman asuntorakentamisen keskus (Varke)</li>
    <li>PL 35, 00023 Valtioneuvosto</li>
    <li>Puhelin: 0295 16001 (vaihde)</li>
    <li>Sähköposti: varke.ym(at)gov.fi</li>
  </ul>

  <H2 text="Yhteyshenkilö" />
  <ul class="contact-info">
    <li>Kimmo Huovinen, apulaisjohtaja</li>
    <li>Valtion tukeman asuntorakentamisen keskus (Varke)</li>
    <li>etunimi.sukunimi(at)gov.fi</li>
  </ul>
</div>
