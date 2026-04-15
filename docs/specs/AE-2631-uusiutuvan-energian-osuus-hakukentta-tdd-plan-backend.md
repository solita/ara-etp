# TDD-suunnitelma: Uusiutuvan energian osuus — hakukentän SQL-lauseke

## Muutettava testitiedosto

Kaikki testit muokataan olemassa olevaan tiedostoon:
`etp-core/etp-backend/src/test/clj/solita/etp/service/energiatodistus_search_test.clj`

Nykyiset stub-testit `search-by-uusiutuvan-energian-osuus-stub-zero-test` ja `search-by-uusiutuvan-energian-osuus-stub-nonzero-test` **korvataan** alla olevilla testeillä.

---

## Testiskenaariot

### 1. Peruslaskenta tunnetuilla arvoilla (ET2026)

**Testi:** `search-by-uusiutuvan-energian-osuus-2026-test`

**Korvaa:** `search-by-uusiutuvan-energian-osuus-stub-zero-test`

- Luodaan ET2026-todistus, jolla on tunnetut arvot:
  - `lammitetty-nettoala` (esim. 100)
  - `e-luku` (esim. 150)
  - `uusiutuvat-omavaraisenergiat-kokonaistuotanto`: aurinkosahko, tuulisahko, aurinkolampo
  - `uusiutuvat-omavaraisenergiat`: aurinkosahko, tuulisahko, aurinkolampo (hyödynnetyt)
- Lasketaan odotettu arvo testissä samalla kaavalla kuin Clojure-funktio (mutta numeerisena):
  - `osoittaja = Σ(kokonaistuotanto × kerroin) / nettoala`
  - `nimittäjä = e-luku + Σ(hyödynnetty × kerroin) / nettoala`
  - `tulos = round(osoittaja / nimittäjä × 100)`
- Haetaan `= tulos` → todistus löytyy

### 2. Nolla nettoala (ET2026)

**Testi:** `search-by-uusiutuvan-energian-osuus-zero-nettoala-test`

- Luodaan ET2026-todistus normaaliarvoilla, allekirjoitetaan
- Päivitetään `lt$lammitetty_nettoala = 0` suoralla SQL:llä (validointi estää 0:n insertissä)
- Haetaan `= 0` → kyseinen todistus **ei** löydy (NULL ei matchi)
- Haetaan millä tahansa arvolla → kyseinen todistus **ei** löydy

### 3. NULL e-luku (ET2026)

**Testi:** `search-by-uusiutuvan-energian-osuus-null-e-luku-test`

- Luodaan ET2026-todistus, jolla on uusiutuvan energian tuotantoarvot, mutta `e-luku` on NULL
- Päivitetään `t$e_luku = NULL` suoralla SQL:llä
- Haetaan `= 0` → todistus **ei** löydy (SQL palauttaa NULL kun e-luku puuttuu)

### 4. Puuttuvat (NULL) tuotanto- ja hyödyntämisarvot (ET2026)

**Testi:** `search-by-uusiutuvan-energian-osuus-null-tuotantoarvot-test`

- Luodaan ET2026-todistus, jossa:
  - Osa kokonaistuotanto-arvoista on nil (esim. vain aurinkosahko on asetettu, tuulisahko ja aurinkolampo nil)
  - Vastaavasti osa hyödynnetty-arvoista nil
- Lasketaan odotettu arvo niin, että nil-arvot ovat 0 (coalesce-käyttäytyminen)
- Haetaan `= odotettu arvo` → todistus löytyy

### 5. Kaikki tuotantoarvot nolla (ET2026)

**Testi:** `search-by-uusiutuvan-energian-osuus-zero-tuotanto-test`

- Luodaan ET2026-todistus, jossa kaikki kokonaistuotanto-arvot ovat 0 (tai nil)
- Odotettu tulos: osoittaja = 0, joten tulos = 0
- Haetaan `= 0` → todistus löytyy

### 6. Nimittäjä nolla (e-luku 0, hyödynnetyt 0)

**Testi:** `search-by-uusiutuvan-energian-osuus-zero-nimittaja-test`

- Luodaan ET2026-todistus, jossa:
  - `e-luku = 0`
  - Kaikki hyödynnetty-arvot ovat 0 tai nil
  - Kokonaistuotannossa on ei-nolla arvoja
- Päivitetään e-luku 0:ksi suoralla SQL:llä
- Haetaan `= 0` → todistus **ei** löydy (nimittäjä 0 → NULL, ei nollalla jakamista)

### 7. Vertailuoperaattorit (>, <)

**Testi:** `search-by-uusiutuvan-energian-osuus-comparison-operators-test`

- Luodaan ET2026-todistus tunnetuilla arvoilla, lasketaan odotettu uusiutuvan energian osuus
- `< (odotettu - 1)` → todistus **ei** löydy
- `> (odotettu + 1)` → todistus **ei** löydy
- `> (odotettu - 1)` → todistus **löytyy**

### 8. Vanha todistusversio (ET2018) palauttaa NULL

**Testi:** `search-by-uusiutuvan-energian-osuus-et2018-returns-null-test`

**Korvaa:** `search-by-uusiutuvan-energian-osuus-stub-nonzero-test`

- Luodaan ET2018-todistus (vanha versio)
- Haetaan `= 0` → todistus **ei** löydy (CASE WHEN versio = 2026 palauttaa NULL muilleversioille)
- Haetaan millä tahansa arvolla → todistus **ei** löydy

---

## Huomiot

- Kertoimet testeissä: aurinkosähkö 0.90, tuulisähkö 0.90, aurinkolämpö 0.38
- Testien tulee laskea odotettu arvo testikoodissa (ei kovakoodattuja lukuja), jotta SQL-lausekkeen oikeellisuus voidaan varmistaa
- `kasvihuonepaastot-per-nelio`-testit (rivit 1262–1372) toimivat rakenteellisena referenssinä
- Testidatan luomisessa voi hyödyntää olemassa olevaa `test-data-set-2026`-funktiota, mutta edgecaseille tarvitaan räätälöityä testidataa (kuten `kasvihuonepaastot-per-nelio-null-values-test` tekee)
