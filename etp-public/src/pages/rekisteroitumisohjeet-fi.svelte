<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import H2 from '@Component/H2';

  onMount(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'smooth'
    });
  });

  announceAssertively($_('REKISTEROITYMISOHJEET'));
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

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('REKISTEROITYMISOHJEET')}"
  descriptionFi={$locale == 'fi' ? $_('REOHJ_INFO_TITLE') : undefined}
  descriptionSv={$locale == 'sv' ? $_('REOHJ_INFO_TITLE') : undefined} />

<Container {...containerStyles.beige}>
  <InfoBlock title={$_('REOHJ_INFO_TITLE')}>{$_('REOHJ_INFO_TEXT')}</InfoBlock>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <div class="border-b border-grey pb-8 mb-8">
      <H2>{$_('REOHJ_KUINKA_LISATAAN')}</H2>
      <p>{$_('REOHJ_PATEVYYSKOKEEN_SUORITTANEET')}</p>
    </div>
    <div class="border-b border-grey pb-8 mb-8">
      <H2>{$_('REOHJ_KUINKA_KAYTTAMAAN')}</H2>
      <ol class="numbered-list">
        <li>{$_('REOHJ_JALKEEN_1')}</li>
        <li>{$_('REOHJ_JALKEEN_2')}</li>
      </ol>

      <p class="my-4">{$_('REOHJ_KIRJAUTUMINEN_EI_ONNISTU')}</p>
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
