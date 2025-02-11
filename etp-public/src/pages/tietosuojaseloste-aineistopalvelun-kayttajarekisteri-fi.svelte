<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  let component;
  announceAssertively(
    $_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')
  );

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

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_(
    'TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI'
  )}"
  descriptionFi={$locale === 'fi'
    ? $_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')
    : undefined}
  descriptionSv={$locale === 'sv'
    ? $_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')
    : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <H2>{$_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')}</H2>

      <H3>Henkilötietojen käyttötarkoitus ja käsittelyperuste</H3>

      <p>
        Henkilötietojen käsittely perustuu Varken ja tietoluvan saajan (asiakkaan)
        väliseen sopimukseen Aineistopalvelun käytöstä. Rekisterissä käsitellään
        asiakkaan teknisen yhteyshenkilön henkilötietoja rajapintayhteyden
        muodostamiseksi asiakkaan tietoverkkoon, ja ongelmatilanteiden
        ratkaisemiseen. Lisäksi tietoja käytetään käyttövaltuuksien
        hallinnointiin ja seurantaan sekä lokitietojen keräämiseen.
      </p>

      <H3>Mitä henkilötietoja käsitellään?</H3>

      <p>
        Käsittelemme rekisterissä asiakkaan teknisen yhteyshenkilön tietoja:
      </p>

      <ul>
        <li>nimi</li>
        <li>sähköpostiosoite</li>
        <li>yritys</li>
        <li>IP-osoite, johon tietoaineisto rajapinnan kautta luovutetaan.</li>
      </ul>

      <p>Henkilötiedot saadaan asiakkaalta.</p>

      <H3>Henkilötietojen käsittely, siirto ja luovuttaminen</H3>

      <p>
        Rekisterin tiedot on suojattu asiattomalta katselulta, muuttamiselta ja
        hävittämiseltä. Suojaus perustuu käyttövaltuushallintaan sekä teknisiin
        suojausmenetelmiin, joilla tietojärjestelmiä suojataan. Pääsy- ja
        käsittelyoikeus tietoihin on työtehtävien perusteella. Pääsy
        järjestelmään perustuu henkilökohtaisiin käyttäjätunnuksiin. Laitetilat
        ja tiedot sijaitsevat fyysisesti EU:n alueella.
      </p>

      <p>
        Tietoja ei säännönmukaisesti luovuteta mihinkään. Tietoja voidaan
        luovuttaa ainoastaan voimassa olevan lainsäädännön velvoittamissa
        tapauksissa.
      </p>

      <p>
        Varke on ulkoistanut energiatodistusrekisterin ja siihen liittyvän
        Aineistopalvelun teknisen ylläpidon ja kehittämisen
        IT-palveluntarjoajalle, jonka kanssa on solmittu tietosuoja-asetuksen
        mukainen sopimus henkilötietojen käsittelystä.
      </p>

      <p>
        Tietoja ei siirretä EU:n tai ETA:n ulkopuolelle eikä kansainvälisille
        järjestöille.
      </p>

      <H3>Tietojen säilytysaika</H3>

      <p>
        Henkilötietoja säilytetään Varken ja asiakkaan sopimuksen voimassaolon
        ajan. Teknisen yhteyshenkilön vaihtuessa kesken sopimuskauden uusi
        tekninen yhteyshenkilö lisätään rekisteriin ja edeltäjän tiedot
        poistetaan rekisteristä.
      </p>

      <H3>Rekisteröidyn oikeudet</H3>

      <p>
        Rekisteröidyllä on oikeus saada rekisterinpitäjältä vahvistus siitä,
        että häntä koskevia henkilötietoja käsitellään tai että niitä ei
        käsitellä, ja jos näitä henkilötietoja käsitellään, oikeus saada pääsy
        henkilötietoihin.
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
        Jos rekisteri sisältää virheellisiä henkilötietoja, rekisteröity voi
        pyytää oikaisua rekisterinpitäjältä, joka korjaa tiedot oikeaksi.
      </p>

      <p>
        Rekisteröity voi pyytää tietojensa poistamista, jos hän ei enää toimi
        teknisenä yhteyshenkilönä työtehtävien muutoksesta tai muusta syystä
        johtuen.
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
