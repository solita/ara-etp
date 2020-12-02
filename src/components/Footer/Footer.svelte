<script>
  import Button from '@Component/Button/Button';
</script>

<footer class="flex flex-col w-full text-sm">
  <div class="flex flex-col md:flex-row justify-between p-8">
    <div class="flex flex-col space-y-4">
      <span class="uppercase font-bold text-secondary">
        Viesti paakayttajalle
      </span>
      <span class="font-bold text-secondary">
        Onko onglemia palvelun kaytossa?
      </span>
      <Button style="primary" text={'Laheta viesti'} />
    </div>
    <div class="flex flex-col space-y-4">
      <span class="uppercase font-bold text-secondary">
        Tietoa sivustosta
      </span>
      <a class="font-bold text-primary" href="/"> Rekisteriseloste </a>
    </div>
    <div class="flex flex-col text-primary">
      <span class="uppercase font-bold text-secondary"> yhteystiedot </span>
      <span class="font-bold"> asumisen rahoitus ja kehittamiskeskus </span>
      <span class="font-bold"> PL 30 </span>
      <span class="font-bold"> 15141 </span>
      <br />
      <span class="font-bold"> Puhelin: 029 525 0800 </span>
      <span class="font-bold"> Sahkoposti: energiatodistus@ara.fi </span>
    </div>
  </div>
  <div class="w-full border-t border-tableborder mx-4 pt-4 pb-16">
    <img class="text-primary" src="images/ara_logo_simple_nega.svg" alt="Ara" />
  </div>
</footer>
