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
      <H2>UNDERTECKNING AV ENERGICERTIFIKAT</H2>
      <p>
        Energicertifikatet undertecknas elektroniskt. Det finns två sätt att
        underteckna certifikatet:
      </p>
      <ol class="numbered-list">
        <li>
          <p>
            Underteckning baserad på <b>Suomi.fi-autentisering</b>, där
            systemets underteckningscertifikat används. Autentisering sker när
            du loggar in i registret. När du undertecknar certifikatet behöver
            du inte autentisera dig igen. Autentisering krävs endast vid
            undertecknande om det har gått mer än 1,5 timmar sedan inloggningen.
          </p>
        </li>
        <li>
          <p>
            Underteckning med <b>elektroniskt identitetskort</b> (certifikatkort),
            där det personliga underteckningscertifikatet används. För detta behöver
            du ett ID-kort, en kortläsare och kortläsarprogrammet.
          </p>
          <ul>
            <li>
              <p>
                Kortet ansöks hos polisen:
                <Link href="https://poliisi.fi/etusivu">
                  Gå till polisens webbplats för att ansöka om identitetskort
                </Link>
              </p>
            </li>
            <li>
              <p>Kortläsaren kan köpas i elektronikbutiker.</p>
            </li>
            <li>
              <p>
                Kortläsarprogrammet kan laddas ner och installeras från
                Myndigheten för digitalisering och befolkningsdata.
                <Link href="https://dvv.fi/kortinlukijaohjelmisto">
                  Gå till sidan för kortläsarprogrammet (dvv.fi)
                </Link>
              </p>
            </li>
          </ul>
        </li>
      </ol>
      <p>
        Mer information om elektronisk underteckning och certifikatkort finns på
        Myndigheten för digitalisering och befolkningsdatas webbplats:
        <Link href="https://dvv.fi/varmenteet"
          >certifikat och kort (dvv.fi)</Link>
      </p>
    </div>
  </div>
</Container>
