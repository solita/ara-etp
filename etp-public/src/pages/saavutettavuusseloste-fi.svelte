<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import Link from '@Component/link';
  import H2 from '@Component/H2';
  import H3 from '@Component/H3';

  let component;
  announceAssertively($_('SAAVUTETTAVUUSSELOSTE'));

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    list-style: disc;
    @apply my-4;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('SAAVUTETTAVUUSSELOSTE')}"
  descriptionFi={$locale == 'fi' ? $_('SAAVUTETTAVUUSSELOSTE') : undefined}
  descriptionSv={$locale == 'sv' ? $_('SAAVUTETTAVUUSSELOSTE') : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <H2>{$_('SAAVUTETTAVUUSSELOSTE')}</H2>
      <p>
        Varke pyrkii takaamaan energiatodistusrekisteri.fi-palvelun
        saavutettavuuden Lain digitaalisten palvelujen tarjoamisesta 306/2019
        mukaisesti.
      </p>
      <p>
        Tämä saavutettavuusseloste koskee energiatodistusrekisteri.fi-sivustoa.
      </p>
      <H3>Saavutettavuuden tila</H3>
      <p>
        energiatodistusrekisteri.fi täyttää Verkkosisällön
        saavutettavuusohjeiden (WCAG) 2.1 tason AA vaatimukset osittain.
      </p>
      <H3>Ei-saavutettava sisältö</H3>
      <p>
        Jäljempänä mainittu sisältö ei ole saavutettavissa seuraavista syistä:
      </p>
      <p>
        Lain digitaalisten palvelujen tarjoamisesta 306/2019 noudattamatta
        jättäminen
      </p>
      <ul class="ml-6">
        <li>
          Päävalikon linkeissä ikonin tekstivastine toistaa turhaan
          linkkitekstin sisällön (WCAG 1.1.1)
        </li>
        <li>
          Otsikko–arvo-pareja ei ole esitetty ohjelmallisesti. (WCAG 1.3.1)
        </li>
        <li>
          Haku tapahtuu automaattisesti hakukentästä poistuttaessa tai
          Enterillä, mutta ruudunlukijakäyttäjä ei saa palautetta. (WCAG 1.3.1)
        </li>
        <li>
          Yläpalkin logo turhaan &#60;h1&#62;-elementin sisällä. (WCAG 1.3.1)
        </li>
        <li>
          Otsikoiden tasot eivät etene loogisesti, useampi h1-tason otsikko ja
          loogisesti toisilleen alisteisia otsikoita, joilla sama otsikkotaso.
          (WCAG 1.3.1)
        </li>
        <li>
          Suodatusvalintojen vihjeteksti ei sulkeudu Esciä painamalla, voi jäädä
          näkyviin, vaikka kohdistus siirtyy muualle. Valintapainikkeiden
          toiminta poikkeaa totutusta. (WCAG 1.4.13)
        </li>
        <li>
          Osa visuaalisista elementeistä ei skaalaudu siististi vain tekstiä
          suurennettaessa. (WCAG 1.4.4)
        </li>
        <li>Kohdistus ei aina toimi loogisesti. (WCAG 2.4.3)</li>
        <li>
          Lomakeryhmien näkyvä nimilappu ja saavutettava nimi ei aina täsmää.
          (WCAG 2.5.3)
        </li>
        <li>
          Suodattimet muokkaavat automaattisesti tuloslistaa, mutta siitä ei
          ilmoiteta käyttäjälle etukäteen. (WCAG 3.2.2)
        </li>
        <li>
          Kaikkia hakukenttiä ei ole merkitty ymmärrettävästi. (WCAG 3.3.2)
        </li>
        <li>
          Pätevyystason valinnan ohjeita ei aina lueta ruudunlukijalla. (WCAG
          3.3.2)
        </li>
        <li>Nav-elementeillä ei ole yksilöivää nimeä. (WCAG 4.1.2)</li>
        <li>
          Sivusiirtymistä ei aina saa ruudunlukuohjelmalla palautetta. (WCAG
          4.1.3)
        </li>
      </ul>
      <h2>Tämän saavutettavuusselosteen laatiminen</h2>
      <p>
        Tämä seloste on laadittu 25.4.2022 ja sitä on päivitetty 27.02.2023.
      </p>
      <p>
        Seloste perustuu ulkopuolisen asiantuntijaorganisaation (Eficode Oy)
        toteuttamaan arviointiin siitä, täyttääkö verkkosivusto
        <Link
          href="https://www.finlex.fi/fi/laki/alkup/2019/20190306"
          referrerpolicy="no-referrer"
          target="_blank">
          Lain digitaalisten palvelujen tarjoamisesta 306/2019
        </Link>
        vaatimukset.
      </p>
      <H3>Palaute ja yhteystiedot</H3>
      <p>
        Jos huomasit saavutettavuuspuutteen, jota ei tällä sivulla ole kuvattu,
        kerrothan sen meille. Ota yhteyttä palautelomakkeella:
        <Link
          href="https://www.varke.fi/fi/form/feedback-form
"
          referrerpolicy="no-referrer"
          target="_blank">
          Anna palautetta (varke.fi)
        </Link>.
      </p>
      <p>
        Vastaamme viestiisi niin pian kuin mahdollista. Huomaathan, että
        vastauksessamme voi kuitenkin kulua jopa 14 päivää.
      </p>

      <H3>Täytäntöönpanomenettely</H3>

      <p>
        Liikenne- ja viestintävirasto valvoo lain edellyttämien
        saavutettavuusvaatimusten toteutumista. Jos huomaat sivustolla
        saavutettavuusongelmia, anna ensin palautetta meille eli sivuston
        ylläpitäjälle.
      </p>

      <p>
        Jos et ole tyytyväinen meiltä saamaasi vastaukseen tai et saa vastausta
        lainkaan kahden viikon kuluessa, voit tehdä ilmoituksen Liikenne- ja
        viestintäviraston digitaalisen esteettömyyden ja saavutettavuuden
        valvontaan. Liikenne- ja viestintäviraston sivulla kerrotaan, miten
        ilmoituksen voi tehdä ja miten asia käsitellään.
      </p>

      <p>
        <Link
          href="https://www.saavutettavuusvaatimukset.fi/fi/kayttajan-oikeudet/palvelun-kayttajan-oikeudet"
          >Palvelun käyttäjän oikeudet (saavutettavuusvaatimukset.fi)</Link>
      </p>

      <H3>Valvontaviranomaisen yhteystiedot</H3>
      <ul class="contact-info">
        <li>Liikenne- ja viestintävirasto Traficom</li>
        <li>Digitaalisen esteettömyyden ja saavutettavuuden valvontayksikkö</li>
        <li>
          Verkkosivut: <Link
            href="https://www.saavutettavuusvaatimukset.fi/"
            target="_blank">Saavutettavuusvaatimukset.fi</Link>
        </li>
        <li>
          Sähköposti: <Link href="mailto:saavutettavuus@traficom.fi"
            >saavutettavuus@traficom.fi</Link>
        </li>
        <li>Puhelin: 029 534 5000 (vaihde)</li>
      </ul>
    </div>
  </div>
</Container>
