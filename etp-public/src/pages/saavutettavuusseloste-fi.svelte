<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import Link from '@Component/link';

  let component;
  announceAssertively($_('SAAVUTETTAVUUSSELOSTE'));

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  h2 {
    @apply mb-2 mt-8;
  }

  ul {
    list-style: disc;
    @apply my-4;
  }

  .contact-info {
    @apply list-none;
  }

  h3 {
    @apply font-bold;
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
      <h2 class="mb-4">{$_('SAAVUTETTAVUUSSELOSTE')}</h2>
      <p>
        ARA pyrkii takaamaan energiatodistusrekisteri.fi-palvelun
        saavutettavuuden Lain digitaalisten palvelujen tarjoamisesta 306/2019
        mukaisesti.
      </p>
      <p>
        Tämä saavutettavuusseloste koskee energiatodistusrekisteri.fi-sivustoa.
      </p>
      <h3>Saavutettavuuden tila</h3>
      <p>
        energiatodistusrekisteri.fi täyttää Verkkosisällön
        saavutettavuusohjeiden (WCAG) 2.1 tason AA vaatimukset osittain.
      </p>
      <h3>Ei-saavutettava sisältö</h3>
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
      <h3>Palaute ja yhteystiedot</h3>
      <p>
        Jos huomasit saavutettavuuspuutteen, jota ei tällä sivulla ole kuvattu,
        kerrothan sen meille. Ota yhteyttä lähettämällä sähköpostia
        osoitteeseen: viestinta.ara (at) ara.fi
      </p>
      <p>
        Vastaamme viestiisi niin pian kuin mahdollista. Huomaathan, että
        vastauksessamme voi kuitenkin kulua jopa 14 päivää.
      </p>

      <h3>Täytäntöönpanomenettely</h3>
      <p>
        Jos huomaat sivustolla saavutettavuusongelmia, anna ensin palautetta
        sivuston ylläpitäjälle. Vastauksessa voi mennä 14 päivää. Jos et ole
        tyytyväinen saamaasi vastaukseen tai et saa vastausta lainkaan kahden
        viikon aikana, voit antaa palautteen Etelä-Suomen aluehallintovirastoon.
      </p>

      <h3>Valvontaviranomaisen yhteystiedot</h3>
      <ul class="contact-info">
        <li>Saavutettavuuden valvonnan yksikkö</li>
        <li>
          <Link href="https://www.saavutettavuusvaatimukset.fi/" target="_blank"
            >Saavutettavuusvaatimukset.fi</Link>
        </li>
        <li>saavutettavuus(at)avi.fi</li>
        <li>vaihde 0295 016 000</li>
      </ul>
    </div>
  </div>
</Container>
