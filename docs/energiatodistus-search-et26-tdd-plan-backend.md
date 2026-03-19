# TDD-suunnitelma: ET2026-hakukentät ja CSV-sarakkeet (Backend)

Perustuu spesifikaatioon: `energiatodistus-search-et26.spec.md`

---

## Osa 1: Hakukentät — `private-search-schema` (energiatodistus_search_test.clj)

> **Muokataan olemassa olevaa testitiedostoa:**
> `etp-core/etp-backend/src/test/clj/solita/etp/service/energiatodistus_search_test.clj`

### 1.1 Haku: `perustiedot.havainnointikayntityyppi-id`

- Testiskenaario: Lisätään ET2026-todistus, jolla on tietty `havainnointikayntityyppi-id`. Haetaan tällä arvolla — todistus löytyy.
- Testiskenaario: Haetaan väärällä `havainnointikayntityyppi-id`-arvolla — todistusta ei löydy.
- Reunaehto: ET2018-todistuksella ei ole tätä kenttää — haku tällä kentällä ei palauta ET2018-todistuksia.

### 1.2 Haku: `lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`

- Testiskenaario: Lisätään ET2026-todistus, jossa kenttä on `true`. Haetaan `= true` — löytyy.
- Testiskenaario: Haetaan `= false` — ei löydy (kun data on `true`).

### 1.3 Haku: `lahtotiedot.lammitys.lammonjako-lampotilajousto`

- Testiskenaario: Haetaan `= true` / `= false` boolean-kentällä — oikea tulos palautuu.

### 1.4 Haku: `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.*`

Jokainen alikent (`aurinkosahko`, `aurinkolampo`, `tuulisahko`, `lampopumppu`, `muulampo`, `muusahko`) testataan yhdellä numeerisella vertailulla:

- Testiskenaario: Haetaan `=`-operaattorilla tietyllä arvolla — todistus löytyy.
- Testiskenaario: Haetaan `>`-operaattorilla arvoa suuremmalla — todistusta ei löydy.

### 1.5 Haku: `toteutunut-ostoenergiankulutus` — uudet kentät

Kentät: `tietojen-alkuperavuosi`, `lisatietoja-fi`, `lisatietoja-sv`, `uusiutuvat-polttoaineet-vuosikulutus-yhteensa`, `fossiiliset-polttoaineet-vuosikulutus-yhteensa`, `uusiutuva-energia-vuosituotto-yhteensa`.

- Testiskenaario: Haetaan numeerisella vertailulla (vuosikulutus-kentät) — oikea tulos.
- Testiskenaario: Haetaan merkkijonohaulla (`lisatietoja-fi`) — oikea tulos.
- Testiskenaario: Haetaan numeerisella vertailulla (`tietojen-alkuperavuosi`) — oikea tulos.

### 1.6 Haku: `huomiot.*.toimenpide.*.kasvihuonepaastojen-muutos`

- Testiskenaario: Lisätään ET2026-todistus, jossa tietyn huomiokategorian toimenpiteellä on `kasvihuonepaastojen-muutos`-arvo. Haetaan tällä arvolla — todistus löytyy.
- Reunaehto: ET2018-todistuksella ei ole tätä kenttää — haku ei palauta ET2018-todistusta.

### 1.7 Haku: `huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina`

- Testiskenaario: Haetaan numeerisella vertailulla — oikea tulos.

### 1.8 Haku: `ilmastoselvitys.*` — peruskentät

Kentät: `laatimisajankohta`, `laatija`, `yritys`, `yritys-osoite`, `yritys-postinumero`, `yritys-postitoimipaikka`, `laadintaperuste`.

- Testiskenaario (`laatimisajankohta`): Haetaan päivämäärävertailulla (`=`) — oikea tulos.
- Testiskenaario (`laatija`, `yritys`, `yritys-osoite`, `yritys-postitoimipaikka`): Haetaan merkkijonohaulla (`ilike`) — oikea tulos.
- Testiskenaario (`yritys-postinumero`): Haetaan merkkijonohaulla — oikea tulos.
- Testiskenaario (`laadintaperuste`): Haetaan luokitteluarvolla — oikea tulos.
- Reunaehto: Haku ilmastoselvityskentillä ET2018-todistusta vastaan ei palauta tuloksia.

### 1.9 Haku: `ilmastoselvitys.hiilijalanjalki.*` ja `ilmastoselvitys.hiilikadenjalki.*`

Jokainen alikent (`rakennus.rakennustuotteiden-valmistus`, `rakennus.kuljetukset-tyomaavaihe`, jne.) testataan yhdellä edustavalla testillä:

- Testiskenaario: Haetaan numeerisella `=`-vertailulla — todistus löytyy.
- Testiskenaario: Haetaan väärällä arvolla — todistusta ei löydy.
- Riittää testata kustakin tasosta (`hiilijalanjalki.rakennus`, `hiilijalanjalki.rakennuspaikka`, `hiilikadenjalki.rakennus`, `hiilikadenjalki.rakennuspaikka`) 1–2 edustava kenttä, ei kaikkia yksittäin.

### 1.10 Hakutulosten autorisointi ET2026-todistuksille

- Testiskenaario: Laatija hakee ET2026-kentillä — näkee vain omat todistuksensa.
- Testiskenaario: Pääkäyttäjä hakee ET2026-kentillä — näkee allekirjoitetut todistukset.
- Tämä testaa, että `whoami->sql`-rajaus toimii oikein myös kun haetaan ET2026-spesifisillä kentillä.

---

## Osa 2: CSV-sarakkeet — `private-columns` (energiatodistus_csv_test.clj)

> **Muokataan olemassa olevaa testitiedostoa:**
> `etp-core/etp-backend/src/test/clj/solita/etp/service/energiatodistus_csv_test.clj`

### 2.1 Sarakevalidointi: ET2026-polut löytyvät `private-columns`-listasta

Olemassa oleva `columns-test` validoi, että kaikki generoidun energiatodistuksen polut löytyvät `private-columns`-sarakkeista. Testi generoi jo ET2018-dataa.

- Testiskenaario: Laajennetaan olemassa oleva `columns-test` kattamaan myös ET2026-generoidut todistukset (samalla logiikalla kuin nykyinen ET2018-testi). Testi epäonnistuu jos `private-columns` ei sisällä ET2026-polkuja.
- Reunaehto: Dissoc-polut tulee päivittää vastaamaan ET2026:n erityistapauksia (esim. `havainnointikayntityyppi-fi`, `havainnointikayntityyppi-sv`, `havainnointikayntityyppi-id` ovat `complete-energiatodistus`-funktiossa lisättyjä kenttiä jotka eivät mene CSV:hen, `e-luokka-rajat`, `kuukausierittely` jne.)

### 2.2 CSV-tuloste sisältää ET2026-sarakkeet otsikkorivillä

- Testiskenaario: Generoidaan ET2026-todistuksia, ladataan CSV `energiatodistukset-private-csv`-funktiolla. Tarkistetaan, että otsikkorivi sisältää ET2026-spesifisiä sarakeotsikoita (esim. ilmastoselvityskentät).

### 2.3 CSV-tuloste: ET2026-todistuksen arvo näkyy oikeassa sarakkeessa

- Testiskenaario: Luodaan ET2026-todistus tunnetulla `ilmastoselvitys.laatija`-arvolla. Ladataan CSV ja tarkistetaan, että arvo löytyy oikeasta sarakkeesta kyseisen todistuksen riviltä.

### 2.4 CSV-tuloste: ET2018-todistuksen uudet sarakkeet ovat tyhjiä

- Testiskenaario: Generoidaan sekä ET2018- että ET2026-todistuksia (olemassa oleva `test-data-set` tekee jo tämän). Ladataan CSV. Tarkistetaan, että ET2018-todistuksen rivillä ET2026-spesifiset sarakkeet ovat tyhjiä (`""`), mutta ET2026-rivillä niissä on arvoja.

### 2.5 CSV-tuloste: `kasvihuonepaastojen-muutos` jokaisessa huomiokategoriassa

- Testiskenaario: Generoidaan ET2026-todistus. Tarkistetaan CSV:stä, että jokaisen huomiokategorian (`iv-ilmastointi`, `valaistus-muut`, `lammitys`, `ymparys`, `alapohja-ylapohja`) jokaisen toimenpiteen (`0`, `1`, `2`) `kasvihuonepaastojen-muutos`-sarake on otsikkorivillä.

### 2.6 CSV-tuloste: `lammitys.kayttoikaa-jaljella-arvio-vuosina`

- Testiskenaario: Tarkistetaan, että CSV-otsikkoriviltä löytyy sarake `huomiot / lammitys / kayttoikaa-jaljella-arvio-vuosina` (tai vastaava formatoitu nimi).

### 2.7 CSV-tuloste: `dissoc-not-in-2026` — `polttoaineet-vuosikulutus-yhteensa` ei ole ET2026-spesifinen sarake

- Testiskenaario: Tarkistetaan, ettei CSV-sarakkeissa ole `toteutunut-ostoenergiankulutus / polttoaineet-vuosikulutus-yhteensa` duplikaattia. Kenttä on jo olemassa ET2018:ssa mutta poistettu ET2026-skeemasta.
