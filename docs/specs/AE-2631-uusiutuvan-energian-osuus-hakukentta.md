# Uusiutuvan energian osuus — hakukentän SQL-lausekkeen toteutus

## Tausta ja tarve

Hakukenttä `energiatodistus.tulokset.uusiutuvan-energian-osuus` on nyt käytössä sekä pääkäyttäjän että laatijan hakuskeemassa (AE-2631). Backend-puolella hakukenttä on kuitenkin **stub**: `energiatodistus_search_fields.clj` palauttaa kovakoodatun arvon `"0"` kaikille todistuksille. Käytännössä tämä tarkoittaa, että haku `= 0` palauttaa kaiken ja mikä tahansa muu ehto ei palauta mitään.

Backendissä on jo olemassa Clojure-funktio `laskennallinen_ostoenergia/uusiutuvan-energian-osuus`, joka laskee oikean arvon PDF-generointia varten. Tämän tehtävän tavoite on toteuttaa vastaava laskenta SQL-lausekkeena, jotta hakumekanismi palauttaa oikeita tuloksia.

## Olemassa oleva laskentalogiikka (Clojure-funktio)

Funktio `uusiutuvan-energian-osuus` sijaitsee namespacessa `solita.etp.service.energiatodistus-pdf.laskennallinen-ostoenergia`.

Laskennan kaava:

```
osoittaja = Σ(kokonaistuotanto × kerroin) / nettoala
nimittäjä = e-luku + Σ(hyödynnetty × kerroin) / nettoala
tulos     = (osoittaja / nimittäjä) × 100
```

Kertoimet (määritelty speksissä, sattuvat yhteen energiamuotokertoimien 2026 kanssa mutta ovat erillinen käsite):
- aurinkosähkö: 0.90
- tuulisähkö: 0.90
- aurinkolämpö: 0.38

Funktion edgecase-käyttäytyminen:
- Palauttaa `nil` jos `versio ≠ 2026`
- Palauttaa `nil` jos `nettoala` on nil, 0 tai negatiivinen
- Palauttaa `nil` jos `e-luku` on nil
- Palauttaa `nil` jos nimittäjä on 0
- Puuttuvat tuotanto-/hyödyntämisarvot käsitellään nollana (`coalesce`)
- Pyöristys kokonaisluvuksi (`Math/round`)

## Muutoksen laajuus

### Backend (`etp-core/etp-backend`)

**Muutettava tiedosto:** `src/main/clj/solita/etp/service/energiatodistus_search_fields.clj`

Nykyinen stub (rivi ~142–143):

```clojure
:uusiutuvan-energian-osuus
["0" common-schema/NonNegative]
```

Tämä korvataan oikealla SQL-lausekkeella, joka kääntää yllä olevan Clojure-kaavan SQL:ksi.

#### SQL-lausekkeen rakenne

Lauseke tarvitsee seuraavat DB-sarakkeet (tulokset-lyhenne on `t`):

**Kokonaistuotanto (osoittaja):**
- `energiatodistus.t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkosahko`
- `energiatodistus.t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$tuulisahko`
- `energiatodistus.t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkolampo`

**Hyödynnetty (nimittäjässä):**
- `energiatodistus.t$uusiutuvat_omavaraisenergiat$aurinkosahko`
- `energiatodistus.t$uusiutuvat_omavaraisenergiat$tuulisahko`
- `energiatodistus.t$uusiutuvat_omavaraisenergiat$aurinkolampo`

**Muut:**
- `energiatodistus.lt$lammitetty_nettoala` (nimittäjänä per-neliö -laskennassa)
- `energiatodistus.t$e_luku` (nimittäjän osa)

SQL-lausekkeen tulee:
1. Käyttää `coalesce(..., 0)` puuttuvien arvojen käsittelyyn (kuten Clojure-funktio käsittelee nil → 0)
2. Käyttää `nullif(..., 0)` nollalla jakamisen estämiseen (sekä nettoala- että kokonaisnimittäjä-tasolla)
3. Palauttaa `NULL` kun nimittäjä on 0 tai nettoala puuttuu/on 0 (vastaa Clojure-funktion nil-palautusta)
4. Tuottaa numeerisen arvon (prosentti kokonaislukuna) — hakukenttä käyttää `NonNegative`-skeemaa

> **Huomio:** Clojure-funktio palauttaa merkkijonon `"24 %"`, mutta hakukentän SQL-lausekkeen tulee palauttaa numeerinen arvo (esim. `24`), koska hakuoperaatiot ovat numeerisia vertailuja (`=`, `>`, `<`).

> **Päätös:** SQL-lausekkeen tulee palauttaa `NULL` vanhoille todistusversioille (ET2013/ET2018), koska uusiutuvan energian osuudelle ei ole määritelmää näissä versioissa. Tämä toteutetaan `CASE WHEN energiatodistus.versio = 2026 THEN ... ELSE NULL END` -rakenteella. Tämä vastaa Clojure-funktion käyttäytymistä, joka palauttaa `nil` kun `versio ≠ 2026`.

#### Vertailu kasvihuonepaastot-per-nelio -toteutukseen

`kasvihuonepaastot-per-nelio` on hyvä referenssitoteutus samassa tiedostossa (rivit 129–141). Se käyttää samaa rakennetta:
- `coalesce` null-käsittelyyn
- `nullif(nettoala, 0)` jakajan suojaukseen
- Kertoimet inline SQL:ssä
- Laskenta yhdessä lausekkeessa

Uusiutuvan energian osuus on rakenteellisesti monimutkaisempi, koska siinä on sekä osoittajassa että nimittäjässä per-nettoala -laskenta, mutta periaate on sama.

**Muutettava testitiedosto:** `src/test/clj/solita/etp/service/energiatodistus_search_test.clj`

Nykyiset stub-testit (`search-by-uusiutuvan-energian-osuus-stub-zero-test` ja `search-by-uusiutuvan-energian-osuus-stub-nonzero-test`) korvataan testeillä, jotka:
- Luovat ET2026-todistuksen tunnetuilla uusiutuvan energian tuotantoarvoilla
- Hakevat todistuksia uusiutuvan energian osuudella ja varmistavat, että haku palauttaa oikeat tulokset
- Kattavat edgecase-tilanteet (nolla nettoala, puuttuvat tuotantoarvot jne.)

### Frontend

**Ei vaadi muutoksia.** Hakukenttä on jo käytössä frontendissä AE-2631:n puitteissa.

### Lokalisointi / i18n

**Ei vaadi muutoksia.** Hakukentän lokalisoinnit ovat jo olemassa.

## Ei-muutokset

- **Clojure-funktio `uusiutuvan-energian-osuus`:** Ei muuteta. Se palvelee eri tarkoitusta (PDF-generointi, palauttaa merkkijonon).
- **Frontend-hakuskeema:** Ei muuteta. Kenttä on jo skeemassa.
- **Muut search fields:** Ei muuteta.

## Riskit ja huomiot

- SQL-lausekkeen tulee tuottaa yhdenmukaisia tuloksia Clojure-funktion kanssa (ottaen huomioon, että SQL palauttaa numeerisen arvon ja Clojure merkkijonon). Tätä vastaavuutta on hyvä varmistaa testeillä.
- Tietokannan sarakerakenne `uusiutuvat_omavaraisenergiat_kokonaistuotanto` on ET2026-spesifi. Vanhemmissa todistusversioissa nämä sarakkeet sisältävät NULL-arvoja, mikä tulee huomioida COALESCE-käsittelyssä.
