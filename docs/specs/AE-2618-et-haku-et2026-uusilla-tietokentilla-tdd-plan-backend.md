# AE-2618: TDD-testisuunnitelma – Backend (Clojure)

## Testitiedostot ja -kehys

- **Kehys:** `clojure.test`
- **Ensisijainen testitiedosto:** `etp-core/etp-backend/src/test/clj/solita/etp/service/energiatodistus_search_test.clj` (olemassa oleva, muokataan)
- **API-tason testitiedosto:** `etp-core/etp-backend/src/test/clj/solita/etp/energiatodistus_search_api_test.clj` (olemassa oleva, muokataan)
- **Apufunktiot:** `test-data-set-2026`, `search-and-assert`, `sign-energiatodistukset!` ovat jo olemassa

---

## 1. Energiatodistus2026 lisäys private-search-schema:an

### Testitiedosto: `energiatodistus_search_test.clj` (muokataan)

### 1.1 ET2026-spesifisellä kentällä haku löytää ET2026-todistuksen

- **Mitä testataan:** Kun `private-search-schema` sisältää `Energiatodistus2026`-skeeman, pääkäyttäjä voi hakea ET2026-spesifisellä kentällä (esim. `energiatodistus.perustiedot.havainnointikayntityyppi-id`) ja haku palauttaa oikean todistuksen.
- **Testidatan valmistelu:** Käytetään `test-data-set-2026`-apufunktiota, asetetaan `havainnointikayntityyppi-id` tunnettuun arvoon.
- **Assertio:** `search-and-assert` palauttaa odotetun todistuksen haettaessa `["=" "energiatodistus.perustiedot.havainnointikayntityyppi-id" <arvo>]`.

### 1.2 ET2026-spesifisellä kentällä haku ei löydä ET2018-todistuksia

- **Mitä testataan:** Haettaessa ET2026-spesifisellä kentällä (esim. `havainnointikayntityyppi-id`) ET2018-todistukset eivät matchaa, koska niiden sarakkeen arvo on NULL.
- **Testidatan valmistelu:** Luodaan sekä ET2018- että ET2026-todistuksia.
- **Assertio:** Hakutulos sisältää vain ET2026-todistukset.

### 1.3 ET2018-kentillä haku löytää edelleen ET2018-todistukset (regressio)

- **Mitä testataan:** Olemassa olevat ET2018-kentillä tehdyt haut toimivat edelleen oikein `Energiatodistus2026`-skeeman lisäämisen jälkeen.
- **Testidatan valmistelu:** Käytetään `test-data-set`-apufunktiota (ET2018-dataa).
- **Assertio:** Olemassa olevat testit (esim. `search-by-id-test`, `search-by-nimi-test`) menevät edelleen läpi.

### 1.4 ET2026-todistus löytyy yhteisillä kentillä haettaessa

- **Mitä testataan:** Kun haetaan kentällä, joka on yhteinen ET2018:lle ja ET2026:lle (esim. `lammitetty-nettoala`), ET2026-todistus löytyy.
- **Testidatan valmistelu:** Luodaan ET2026-todistus tunnetulla `lammitetty-nettoala`-arvolla.
- **Assertio:** Haku `["=" "energiatodistus.lahtotiedot.lammitetty-nettoala" <arvo>]` palauttaa ET2026-todistuksen.

### 1.5 ET2026-todistus rajautuu pois `polttoaineet-vuosikulutus-yhteensa`-hausta

- **Mitä testataan:** Koska `polttoaineet-vuosikulutus-yhteensa` on poistettu ET2026-skeemasta (`dissoc-not-in-2026`), haettaessa tällä kentällä ET2026-todistukset eivät matchaa.
- **Testidatan valmistelu:** Luodaan sekä ET2018- (tunnetulla arvolla) että ET2026-todistuksia.
- **Assertio:** Hakutulos sisältää vain ET2018-todistuksen.

---

## 2. Kokonaistuotanto-kenttien neliövuosikulutus-laskelmat

### Testitiedosto: `energiatodistus_search_test.clj` (muokataan)

### 2.1 Kokonaistuotanto aurinkosähkö neliövuosikulutus

- **Mitä testataan:** Haettaessa `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko-neliovuosikulutus`-kentällä oikealla arvolla (aurinkosahko / nettoala) löydetään ET2026-todistus.
- **Testidatan valmistelu:** Luodaan ET2026-todistus, jossa `uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko` on tunnettu arvo.
- **Assertio:** `search-and-assert` palauttaa oikean todistuksen laskennallisella arvolla.

### 2.2 Kokonaistuotanto aurinkolämpö neliövuosikulutus

- **Mitä testataan:** Sama kuin 2.1, mutta `aurinkolampo`-kentälle.
- **Assertio:** Haku arvolla `aurinkolampo / nettoala` löytää oikean todistuksen.

### 2.3 Kokonaistuotanto tuulisähkö neliövuosikulutus

- **Mitä testataan:** Sama kuin 2.1, mutta `tuulisahko`-kentälle.
- **Assertio:** Haku arvolla `tuulisahko / nettoala` löytää oikean todistuksen.

### 2.4 Kokonaistuotanto lämpöpumppu neliövuosikulutus

- **Mitä testataan:** Sama kuin 2.1, mutta `lampopumppu`-kentälle.
- **Assertio:** Haku arvolla `lampopumppu / nettoala` löytää oikean todistuksen.

### 2.5 Kokonaistuotanto muu lämpö neliövuosikulutus

- **Mitä testataan:** Sama kuin 2.1, mutta `muulampo`-kentälle.
- **Assertio:** Haku arvolla `muulampo / nettoala` löytää oikean todistuksen.

### 2.6 Kokonaistuotanto muu sähkö neliövuosikulutus

- **Mitä testataan:** Sama kuin 2.1, mutta `muusahko`-kentälle.
- **Assertio:** Haku arvolla `muusahko / nettoala` löytää oikean todistuksen.

### 2.7 Kokonaistuotanto raakakentällä (ei per neliö) haku

- **Mitä testataan:** Raaka kokonaistuotanto-kenttien (esim. `aurinkosahko`) haku toimii suoraan ilman neliövuosikulutus-laskelmaa.
- **Testidatan valmistelu:** Luodaan ET2026-todistus tunnetulla kokonaistuotannon aurinkosahko-arvolla.
- **Assertio:** Haku arvolla `["=" "energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko" <arvo>]` löytää oikean todistuksen.

### 2.8 Kokonaistuotanto-kentällä haku ei löydä ET2018-todistuksia

- **Mitä testataan:** ET2018-todistuksilla kokonaistuotanto-sarakkeet ovat NULL, joten haku ei palauta niitä.
- **Testidatan valmistelu:** Luodaan sekä ET2018- että ET2026-todistuksia.
- **Assertio:** Hakutulos sisältää vain ET2026-todistukset.

---

## 3. Kasvihuonepäästöt per neliö (computed field)

### Testitiedosto: `energiatodistus_search_test.clj` (muokataan)

### 3.1 Kasvihuonepäästöt per neliö laskenta ET2026-todistukselle

- **Mitä testataan:** Computed field `kasvihuonepaastot-per-nelio` laskee oikein kaavalla `summa(energiamuoto * co2-kerroin) / nettoala`.
- **CO2-kertoimet:** kaukolampo=0.059, sahko=0.05, uusiutuva-polttoaine=0.027, fossiilinen-polttoaine=0.306, kaukojaahdytys=0.014.
- **Testidatan valmistelu:** Luodaan ET2026-todistus tunnetuilla `kaytettavat-energiamuodot`-arvoilla ja `lammitetty-nettoala`-arvolla.
- **Assertio:** Haku laskennallisella arvolla löytää oikean todistuksen.

### 3.2 Kasvihuonepäästöt per neliö laskenta ET2018-todistukselle

- **Mitä testataan:** Sama laskenta toimii myös ET2018-todistukselle, koska CO2-kertoimet ovat versioista riippumattomat.
- **Testidatan valmistelu:** Luodaan ET2018-todistus tunnetuilla arvoilla.
- **Assertio:** Laskennallinen arvo matchaa.

### 3.3 Kasvihuonepäästöt per neliö – nolla nettoala ei aiheuta virhettä

- **Mitä testataan:** Kun nettoala on 0, SQL käyttää `nullif`-funktiota ja palauttaa NULL (ei jakoa nollalla -virhettä). Todistus ei matchaa mihinkään numeeriseen vertailuun.
- **Testidatan valmistelu:** Luodaan todistus, jossa `lammitetty-nettoala` = 0 tai NULL.
- **Assertio:** Haku ei kaadu eikä palauta todistusta.

### 3.4 Kasvihuonepäästöt per neliö – NULL-energiamuotoarvot käsitellään coalesce:lla

- **Mitä testataan:** Kun yksittäinen energiamuoto-arvo on NULL, `coalesce` käsittelee sen arvona 0 ja laskenta onnistuu.
- **Testidatan valmistelu:** Luodaan todistus, jossa osa `kaytettavat-energiamuodot`-arvoista on NULL.
- **Assertio:** Laskennallinen arvo ottaa huomioon vain ei-NULL-kentät.

### 3.5 Kasvihuonepäästöt per neliö – vertailuoperaattorit (>, <)

- **Mitä testataan:** Haetaan käyttäen `<`- ja `>`-operaattoreita laskennalliseen arvoon nähden.
- **Assertio:** `<` pienemmällä arvolla ei löydä, `>` suuremmalla arvolla ei löydä, mutta rajat toimivat oikein.

---

## 4. Uusiutuvan energian osuus (stub)

### Testitiedosto: `energiatodistus_search_test.clj` (muokataan)

### 4.1 Uusiutuvan energian osuus stub palauttaa vakioarvon 0

- **Mitä testataan:** Stub-toteutus palauttaa aina arvon 0 kaikille todistuksille.
- **Assertio:** Haku `["=" "energiatodistus.tulokset.uusiutuvan-energian-osuus" 0]` palauttaa kaikki todistukset.

### 4.2 Uusiutuvan energian osuus stub – ei-nolla ei matchaa

- **Mitä testataan:** Koska stub palauttaa aina 0, haku arvolla != 0 ei palauta yhtään todistusta.
- **Assertio:** Haku `["=" "energiatodistus.tulokset.uusiutuvan-energian-osuus" 1]` palauttaa tyhjän tuloksen.

---

## 5. Boolean-kenttien haku

### Testitiedosto: `energiatodistus_search_test.clj` (muokataan)

### 5.1 Haku `energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin = true` – löytää vain true-arvoiset

- **Mitä testataan:** Haettaessa `= true` löydetään vain todistukset, joissa arvo on asetettu trueksi.
- **Testidatan valmistelu:** Luodaan ET2026-todistus, jossa kenttä on `true`, ja toinen jossa se on `false`.
- **Assertio:** Hakutulos sisältää vain true-arvoisen todistuksen.

### 5.2 Haku `energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin = false` – palauttaa myös vanhat versiot

- **Mitä testataan:** Koska kenttä on `NOT NULL DEFAULT false`, ET2018-todistuksilla arvo on `false`. Haku `= false` palauttaa sekä ET2018- että ET2026-todistuksia, joilla arvo on false.
- **Testidatan valmistelu:** Luodaan sekä ET2018- että ET2026-todistuksia.
- **Assertio:** Hakutulos sisältää ET2018-todistukset ja ET2026-todistukset joilla arvo on false.

### 5.3 Haku `lammonjako-lampotilajousto = true` – löytää vain true-arvoiset

- **Mitä testataan:** Vastaava testi `lammonjako-lampotilajousto`-kentälle.
- **Testidatan valmistelu:** Luodaan ET2026-todistus, jossa kenttä on `true`.
- **Assertio:** Hakutulos sisältää vain true-arvoisen todistuksen.

### 5.4 Haku `lammonjako-lampotilajousto = false` – palauttaa myös vanhat versiot

- **Mitä testataan:** Sama `NOT NULL DEFAULT false` -käyttäytyminen kuin 5.2.
- **Assertio:** Hakutulos sisältää sekä vanhat että uudet todistukset joilla arvo on false.

---

## 6. API-tason testit

### Testitiedosto: `energiatodistus_search_api_test.clj` (muokataan)

### 6.1 Pääkäyttäjä voi hakea ET2026-spesifisillä kentillä HTTP-rajapinnan kautta

- **Mitä testataan:** GET `/api/private/energiatodistukset?where=...` palauttaa HTTP 200 ja oikeat tulokset, kun `where` sisältää ET2026-spesifisen kentän (esim. `havainnointikayntityyppi-id`).
- **Testidatan valmistelu:** Luodaan allekirjoitettuja ET2026-todistuksia tunnetuilla arvoilla.
- **Assertio:** HTTP-vastaus on 200 ja tulosten lukumäärä on oikea.

### 6.2 Tuntematon kenttä palauttaa virheen

- **Mitä testataan:** Haettaessa kentällä, jota ei ole skeemassa (esim. `energiatodistus.olematon-kentta`), API palauttaa virhevastauksen.
- **Assertio:** HTTP-vastaus on 400 tai vastaava virhekoodi.

---

## Katselmoinnin status (2026-03-26)

| Testi | Status | Huomio |
|-------|--------|--------|
| 1.1–1.5 Private-search-schema | ✅ Toteutettu ja läpäisee | |
| 2.1–2.8 Kokonaistuotanto | ✅ Toteutettu ja läpäisee | |
| 3.1–3.5 Kasvihuonepäästöt | ✅ Toteutettu ja läpäisee | coalesce/nullif käsittely oikein |
| 4.1–4.2 Uusiutuvan energian osuus (stub) | ✅ Toteutettu ja läpäisee | |
| 5.1–5.4 Boolean-kentät | ✅ Toteutettu ja läpäisee | |
| 6.1–6.2 API-testit | ✅ Toteutettu ja läpäisee | |

**Backend: Ei löydöksiä.** Kaikki toteutus vastaa suunnitelmaa.
