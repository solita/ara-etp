<script>
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Link from '@Component/link';
  import H2 from '@Component/H2';
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

<Container {...containerStyles.beige}>
  <InfoBlock
    title="Anvisningar för användningen av tjänsten för dem som upprättar energicertifikat">
    Endast användare som registrerats i registret över upprättare kan logga in i
    tjänsten och upprätta energicertifikat.
  </InfoBlock>
</Container>

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto">
    <div class="border-b border-grey pb-8 mb-8">
      <H2>Hur införs jag i registret över upprättare?</H2>
      <p>
        Personer som har avlagt behörighetsprovet med godkänt resultat läggs
        till i registret efter att proven har bedömts. Registreringen i
        registret görs av dem som konstaterar behörigheten (FISE eller Kinko).
      </p>
    </div>
    <div class="border-b border-grey pb-8 mb-8">
      <H2>Hur får jag tillgång till tjänsten?</H2>
      <ol class="numbered-list">
        <li>
          <p>Logga in med Suomi.fi-koder.</p>
        </li>
        <li>
          <p>Kontrollera att dina uppgifter är korrekta i tjänsten.</p>
        </li>
      </ol>

      <p class="my-4">
        Om inloggningen inte lyckas är det möjligt att den som konstaterar
        behörigheten ännu inte har registrerat dig i registret över upprättare.
        Vänta tills du får en faktura från den som konstaterar behörigheten.
        Efter fakturans förfallodatum lägger den som konstaterar behörigheten
        till dig i registret över upprättare, om du har erlagt betalningen.
      </p>
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
          >certifikat och kort (dvv.fi)
        </Link>
      </p>
    </div>
  </div>
</Container>
