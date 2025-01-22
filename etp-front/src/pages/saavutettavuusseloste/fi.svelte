<script>
  import { onMount } from 'svelte';
  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Link from '@Component/Link/Link';

  let component;

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
</script>

<style>
  ul {
    @apply my-4 list-disc;
  }

  .contact-info {
    @apply list-none;
  }

  p {
    @apply mb-4;
  }
</style>

<div bind:this={component}>
  <H1 text="Saavutettavuusseloste" />
  <p>
    ARA pyrkii takaamaan private.energiatodistusrekisteri.fi saavutettavuuden
    Lain digitaalisten palvelujen tarjoamisesta 306/2019 mukaisesti.
  </p>
  <p>
    Tämä saavutettavuusseloste koskee private.energiatodistusrekisteri.fi
    -sivustoa.
  </p>

  <H2 text="Saavutettavuuden tila" />
  <p>
    private.energiatodistusrekisteri.fi täyttää Verkkosisällön
    saavutettavuusohjeiden (WCAG) 2.1 tason AA vaatimukset osittain.
  </p>

  <H2 text="Ei-saavutettava sisältö" />
  <p>Jäljempänä mainittu sisältö ei ole saavutettavissa seuraavista syistä:</p>

  <p>
    Lain digitaalisten palvelujen tarjoamisesta 306/2019 noudattamatta
    jättäminen
  </p>

  <ul class="ml-6">
    <li>
      Joillakin ikoneilla on englanninkielinen tekstivastine. (WCAG 1.1.1)
    </li>
    <li>
      Tekstin muotoilupainikkeilla ei ole saavutettavaa nimeä. (WCAG 1.1.1)
    </li>
    <li>
      Navigaatioita ei ole merkitty maamerkeillä johdonmukaisesti. Sivuilla ei
      ole Main-maamerkkiä. (WCAG 1.3.1, 2.4.1)
    </li>
    <li>Sisältöä, joka ei ole otsikko, on merkitty otsikoksi. (WCAG 1.3.1)</li>
    <li>
      Aktiivisia elementtejä, kuten valittua välilehteä tai valittua sivua
      sivunavigaatiosta, ei ole merkitty ohjelmallisesti. (WCAG 1.3.1)
    </li>
    <li>
      Ohjelmallinen toteutus ei paikoin vastaa sivun visuaalista ulkoasua. (WCAG
      1.3.1)
    </li>
    <li>
      Kaikkien hakukenttien nimilappuja (label) ei ole ohjelmallisesti
      yhdistetty kenttiin. (WCAG 1.3.1)
    </li>
    <li>Ruudunlukija ei saa tietoa sivun sisällön muutoksista. (WCAG 1.3.1)</li>
    <li>
      Palvelussa on lomakkeita, joissa pyydetään käyttäjään liittyviä tietoja,
      mutta kenttien tarkkaa tarkoitusta ei ole määritelty. (WCAG 1.3.5)
    </li>
    <li>Joidenkin painikkeiden tila on merkitty vain värillä. (WCAG 1.4.1)</li>
    <li>Sivusto ei skaalaudu pienelle näytölle. (WCAG 1.4.10)</li>
    <li>
      Käyttöliittymäelementtien kontrastit eivät aina ole riittäviä. (WCAG
      1.4.11)
    </li>
    <li>Tekstien kontrastit eivät aina ole riittäviä. (WCAG 1.4.3)</li>
    <li>
      Osa sivun elementeistä ei saa näppäinkohdistusta tai ruudunlukijan
      kohdistusta. (WCAG 2.1.1)
    </li>
    <li>
      Kohdistusjärjestys ei aina ole looginen. Kohdistus ei siirry modaaliin.
      (WCAG 2.4.3)
    </li>
    <li>Sivuilla on useita h1-tason otsikoita. (WCAG 2.4.6)</li>
    <li>
      Näppäimistökohdistus ei näy aina tai näkyy virheellisesti. (WCAG 2.4.7)
    </li>
    <li>Joillakin painikkeilla on englanninkielinen määrite. (WCAG 3.1.2)</li>
    <li>Yritykset-linkki vaihtaa paikkaa navigaatiossa. (WCAG 3.2.3)</li>
    <li>
      Pakolliset kentät on merkitty tähtikuvakkeella, mutta kuvakkeen merkitystä
      ei ole selitetty. (WCAG 3.3.2)
    </li>
    <li>Pudotusvalikkojen roolia ei ole merkitty. (WCAG 4.1.2)</li>
    <li>Komponentin tilaa ei ole aina merkitty. (WCAG 4.1.2)</li>
    <li>
      Ruudunlukija ei saa tietoa virheilmoituksesta tai onnistuneesta
      tallennuksesta. (WCAG 4.1.3)
    </li>
  </ul>

  <H2 text="Tämän saavutettavuusselosteen laatiminen" />
  <p>Tämä seloste on laadittu 25.4.2022 ja sitä on päivitetty 27.02.2023.</p>
  <p>
    Seloste perustuu ulkopuolisen asiantuntijaorganisaation (Eficode Oy)
    toteuttamaan arviointiin siitä, täyttääkö verkkosivusto <Link
      href="https://www.finlex.fi/fi/laki/alkup/2019/20190306"
      text="Lain digitaalisten palvelujen tarjoamisesta 306/2019" /> vaatimukset.
  </p>

  <H2 text="Palaute ja yhteystiedot" />
  <p>
    Jos huomasit saavutettavuuspuutteen, jota ei tällä sivulla ole kuvattu,
    kerrothan sen meille. Ota yhteyttä lähettämällä sähköpostia osoitteeseen:
    viestinta.ara (at) ara.fi
  </p>
  <p>
    Vastaamme viestiisi niin pian kuin mahdollista. Huomaathan, että
    vastauksessamme voi kuitenkin kulua jopa 14 päivää.
  </p>

  <H2 text="Täytäntöönpanomenettely" />
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
      text="Palvelun käyttäjän oikeudet (saavutettavuusvaatimukset.fi)" />
  </p>

  <H2 text="Valvontaviranomaisen yhteystiedot" />
  <ul class="contact-info">
    <li>Liikenne- ja viestintävirasto Traficom</li>
    <li>Digitaalisen esteettömyyden ja saavutettavuuden valvontayksikkö</li>
    <li>
      Verkkosivut: <Link
        href="https://www.saavutettavuusvaatimukset.fi/"
        text="Saavutettavuusvaatimukset.fi"
        target="_blank" />
    </li>
    <li>
      Sähköposti: <Link
        href="mailto:saavutettavuus@traficom.fi"
        text="saavutettavuus@traficom.fi" />
    </li>
    <li>Puhelin: 029 534 5000 (vaihde)</li>
  </ul>
</div>
