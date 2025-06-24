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
        registret görs av <Link href="https://FISE.fi">FISE</Link>, som
        konstaterar behörigheten.
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
  </div>
</Container>
