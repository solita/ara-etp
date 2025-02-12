<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  let component;
  announceAssertively($_('TIETOSUOJASELOSTE_VALVONTATIETOREKISTERI'));

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

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_(
    'TIETOSUOJASELOSTE_VALVONTATIETOREKISTERI'
  )}"
  descriptionFi={$locale === 'fi'
    ? $_('TIETOSUOJASELOSTE_VALVONTATIETOREKISTERI')
    : undefined}
  descriptionSv={$locale === 'sv'
    ? $_('TIETOSUOJASELOSTE_VALVONTATIETOREKISTERI')
    : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <H2>{$_('TIETOSUOJASELOSTE_VALVONTATIETOREKISTERI')}</H2>

      <H3>Henkilötietojen käyttötarkoitus ja käsittelyperuste</H3>

      <p>
        Rekisterissä käsitellään henkilötietoja rakennuksen energiatodistuksesta
        annetun lain (50/2013) 18 §:n ja rakennusten
        energiatodistustietojärjestelmästä annetun lain (147/2015) 6 §:n
        nojalla.
      </p>

      <p>
        Varken lakisääteisenä tehtävänä on valvoa energiatodistuksia ja niiden
        laatijoita. Rekisterissä käsitellään henkilötietoja, jotka tarvitaan
        energiatodistusten valvontatietorekisterin ylläpitämiseksi. Varke valvoo
        energiatodistusten käyttöä ja oikeellisuutta sekä laatijoiden toimintaa.
      </p>

      <H3>Mitä henkilötietoja käsitellään?</H3>

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
        rekisteristä poistettua tietoa, joka on tarpeen valvonnan
        toteuttamiseksi. Vanhentuneiden energiatodistusten osalta rekisteri voi
        sisältää osoitteen ja pysyvän rakennustunnuksen.
      </p>

      <p>
        Rekisterin sisältämät henkilötiedot saadaan rekisteröidyltä itseltään,
        asuntojen myynti-ilmoituksista, energiatodistusten laatijoilta ja
        kiinteistövälitysyrityksiltä. Lisäksi tietoja voidaan tarkistaa ja
        täydentää viranomaisten rekistereistä. Rekisterinpitäjällä on
        lakisääteinen oikeus havainnoida kohdetta paikan päällä.
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
          tietoliikenteen suojaukseen sekä tietojen varmuuskopiointiin.
        </li>
      </ol>

      <p>
        Pääsy- ja käsittelyoikeus tietoihin myönnetään työtehtävien perusteella.
        Pääsy järjestelmään perustuu henkilökohtaisiin käyttäjätunnuksiin.
        Laitetilat ja tiedot sijaitsevat fyysisesti EU:n alueella.
      </p>

      <p>
        Tietojärjestelmän tekninen ylläpito ja kehittäminen on ulkoistettu
        IT-palveluntarjoajalle. Varke on tehnyt palveluntarjoajan kanssa
        tietosuoja-asetuksen mukaisen sopimuksen henkilötietojen käsittelystä.
      </p>

      <p>
        Tietoja voidaan luovuttaa ainoastaan voimassa olevan lainsäädännön
        velvoittamissa ja sallimissa rajoissa. Valvontaan liittyvä tietopyyntö
        voidaan antaa tiedoksi myös tarkastettavan kohteen
        kiinteistönvälittäjälle, jos kohde on myynnissä kiinteistönvälittäjän
        kautta. Jos energiatodistus on rakennuslupa-asiakirja, tietoja voidaan
        luovuttaa rakennusvalvonnalle.
      </p>

      <p>
        Tietoja ei siirretä EU:n tai ETA:n ulkopuolelle eikä kansainvälisille
        järjestöille.
      </p>

      <H3>Tietojen säilytysaika</H3>

      <p>
        Energiatodistusten valvontatietorekisterin sisältämien henkilötietojen
        säilytysajat pohjautuvat Varken tiedonohjaussuunnitelmaan.
      </p>

      <H3>Rekisteröidyn oikeudet</H3>

      <p>
        Rekisteröidyllä on oikeus saada rekisterinpitäjältä vahvistus siitä,
        että häntä koskevia henkilötietoja käsitellään tai että niitä ei
        käsitellä, ja jos näitä henkilötietoja käsitellään, oikeus saada pääsy
        henkilötietoihin. Rekisteröidyllä ei ole itsellään pääsyä
        henkilötietoihin, mutta rekisteröity voi tehdä tarkastuspyynnön
        rekisterinpitäjän edustajalle.
      </p>

      <p>
        Rekisterien sisältämiä tietoja ei käytetä profilointiin eikä tietoihin
        kohdisteta automaattista päätöksentekoa.
      </p>

      <p>
        Jos rekisteröidyn käyttämästä tarkastusoikeudesta on kulunut vähemmän
        kuin yksi vuosi, voi rekisterinpitäjä periä tietojen antamisesta
        aiheutuvat hallinnollisiin kustannuksiin perustuvan maksun.
      </p>

      <p>
        Rekisteröity voi pyytää rekisterinpitäjää oikaisemaan ilman aiheetonta
        viivytystä rekisteröityä koskevat virheelliset henkilötiedot.
        Rekisteröity voi tehdä oikaisupyynnön rekisterinpitäjän edustajalle.
      </p>

      <p>
        Rekisteröidyllä ei ole oikeutta pyytää tietojensa poistamista, koska
        tietojen käsittely perustuu lakiin. Tästä syystä rekisteröidyllä ei
        myöskään ole oikeutta vastustaa henkilötietojensa käsittelyä, tai
        oikeutta saada tietojaan siirretyksi toiseen järjestelmään.
        Henkilötietojen käsittelyä ei myöskään voi pyytää rajoittamaan, koska
        tietoja käsitellään lain nojalla.
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
        <li>Valtion tukeman asuntorakentamisen keskus (Varke)</li>
        <li>PL 35, 00023 Valtioneuvosto</li>
        <li>Puhelin: 0295 16001 (vaihde)</li>
        <li></li>
        <li>Sähköposti: varke.ym(at)gov.fi</li>
      </ul>

      <H3>Yhteyshenkilö</H3>

      <ul class="contact-info">
        <li>Kimmo Huovinen, apulaisjohtaja</li>
        <li>Valtion tukeman asuntorakentamisen keskus (Varke)</li>
        <li>etunimi.sukunimi(at)gov.fi</li>
      </ul>
    </div>
  </div>
</Container>
