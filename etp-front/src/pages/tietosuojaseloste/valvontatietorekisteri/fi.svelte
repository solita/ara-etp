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

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<div bind:this={component}>
  <H1 text="Tietosuojaseloste Energiatodistusten valvontatietorekisteri" />

  <H2 text="Henkilötietojen käyttötarkoitus ja käsittelyperuste" />

  <p>
    Rekisterissä käsitellään henkilötietoja rakennuksen energiatodistuksesta
    annetun lain (50/2013) 18 §:n ja rakennusten
    energiatodistustietojärjestelmästä annetun lain (147/2015) 6 §:n nojalla.
  </p>

  <p>
    Aran lakisääteisenä tehtävänä on valvoa energiatodistuksia ja niiden
    laatijoita. Rekisterissä käsitellään henkilötietoja, jotka tarvitaan
    energiatodistusten valvontatietorekisterin ylläpitämiseksi. Ara valvoo
    energiatodistusten käyttöä ja oikeellisuutta sekä laatijoiden toimintaa.
  </p>

  <H2 text="Mitä henkilötietoja käsitellään?" />

  <p>
    Käsittelemme Energiatodistusten valvontatietorekisterissä rakennusten
    omistajien tietoja:
  </p>

  <ul>
    <li>nimi</li>
    <li>henkilötunnus</li>
    <li>sähköpostiosoite</li>
    <li>osoite</li>
    <li>omistettu rakennus</li>
  </ul>

  <p>Lisäksi käsittelemme energiatodistusten laatijoiden tietoja:</p>

  <ul>
    <li>nimi</li>
    <li>henkilötunnus</li>
    <li>sähköpostiosoite</li>
    <li>osoite</li>
  </ul>

  <p>
    Lisäksi rekisteri voi sisältää energiatodistuksen laatijoita koskevasta
    rekisteristä poistettua tietoa, joka on tarpeen valvonnan toteuttamiseksi.
    Vanhentuneiden energiatodistusten osalta rekisteri voi sisältää osoitteen ja
    pysyvän rakennustunnuksen.
  </p>

  <p>
    Rekisterin sisältämät henkilötiedot saadaan rekisteröidyltä itseltään,
    asuntojen myynti-ilmoituksista, energiatodistusten laatijoilta ja
    kiinteistövälitysyrityksiltä. Lisäksi tietoja voidaan tarkistaa ja täydentää
    viranomaisten rekistereistä. Rekisterinpitäjällä on lakisääteinen oikeus
    havainnoida kohdetta paikan päällä.
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
      suojaukseen sekä tietojen varmuuskopiointiin.
    </li>
  </ol>

  <p>
    Pääsy- ja käsittelyoikeus tietoihin myönnetään työtehtävien perusteella.
    Pääsy järjestelmään perustuu henkilökohtaisiin käyttäjätunnuksiin.
    Laitetilat ja tiedot sijaitsevat fyysisesti EU:n alueella.
  </p>

  <p>
    Tietojärjestelmän tekninen ylläpito ja kehittäminen on ulkoistettu
    IT-palveluntarjoajalle. Ara on tehnyt palveluntarjoajan kanssa
    tietosuoja-asetuksen mukaisen sopimuksen henkilötietojen käsittelystä.
  </p>

  <p>
    Tietoja voidaan luovuttaa ainoastaan voimassa olevan lainsäädännön
    velvoittamissa ja sallimissa rajoissa. Valvontaan liittyvä tietopyyntö
    voidaan antaa tiedoksi myös tarkastettavan kohteen kiinteistönvälittäjälle,
    jos kohde on myynnissä kiinteistönvälittäjän kautta. Jos energiatodistus on
    rakennuslupa-asiakirja, tietoja voidaan luovuttaa rakennusvalvonnalle.
  </p>

  <p>
    Tietoja ei siirretä EU:n tai ETA:n ulkopuolelle eikä kansainvälisille
    järjestöille.
  </p>

  <H2 text="Tietojen säilytysaika" />

  <p>
    Energiatodistusten valvontatietorekisterin sisältämien henkilötietojen
    säilytysajat pohjautuvat Aran tiedonohjaussuunnitelmaan.
  </p>

  <H2 text="Rekisteröidyn oikeudet" />

  <p>
    Rekisteröidyllä on oikeus saada rekisterinpitäjältä vahvistus siitä, että
    häntä koskevia henkilötietoja käsitellään tai että niitä ei käsitellä, ja
    jos näitä henkilötietoja käsitellään, oikeus saada pääsy henkilötietoihin.
    Rekisteröidyllä ei ole itsellään pääsyä henkilötietoihin, mutta rekisteröity
    voi tehdä tarkastuspyynnön rekisterinpitäjän edustajalle.
  </p>

  <p>
    Rekisterien sisältämiä tietoja ei käytetä profilointiin eikä tietoihin
    kohdisteta automaattista päätöksentekoa.
  </p>

  <p>
    Jos rekisteröidyn käyttämästä tarkastusoikeudesta on kulunut vähemmän kuin
    yksi vuosi, voi rekisterinpitäjä periä tietojen antamisesta aiheutuvat
    hallinnollisiin kustannuksiin perustuvan maksun.
  </p>

  <p>
    Rekisteröity voi pyytää rekisterinpitäjää oikaisemaan ilman aiheetonta
    viivytystä rekisteröityä koskevat virheelliset henkilötiedot. Rekisteröity
    voi tehdä oikaisupyynnön rekisterinpitäjän edustajalle.
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
