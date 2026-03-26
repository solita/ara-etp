# AE-2618: TDD-testisuunnitelma – Frontend (Svelte/JS)

## Testitiedostot ja -kehys

- **Kehys:** Jest 29 (`@jest/globals`, `@testing-library/svelte`)
- **Ympäristö:** `@jest-environment jsdom`

---

## 1. Hakuskeeman laajennos ET2026-kentillä

### Testitiedosto: `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema_test.js` (muokataan)

### 1.1 `flattenSchema` sisältää `havainnointikayntityyppi-id`-kentän

- **Mitä testataan:** Kun `schema` sisältää uuden `perustiedot.havainnointikayntityyppi-id`-kentän, `flattenSchema(schema)` tuottaa avaimen `energiatodistus.perustiedot.havainnointikayntityyppi-id`.
- **Assertio:** `flattenSchema(schema)` sisältää avaimen `energiatodistus.perustiedot.havainnointikayntityyppi-id` ja sen tyyppi on `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI`.

### 1.2 `flattenSchema` sisältää `energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`-kentän

- **Mitä testataan:** Boolean-kenttä `lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin` on mukana skeemassa.
- **Assertio:** `flattenSchema(schema)` sisältää avaimen `energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin` ja sen tyyppi on `OPERATOR_TYPES.BOOLEAN`.

### 1.3 `flattenSchema` sisältää `lammonjako-lampotilajousto`-kentän

- **Mitä testataan:** Boolean-kenttä `lahtotiedot.lammitys.lammonjako-lampotilajousto` on mukana skeemassa.
- **Assertio:** `flattenSchema(schema)` sisältää avaimen `energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto` ja sen tyyppi on `OPERATOR_TYPES.BOOLEAN`.

### 1.4 `flattenSchema` sisältää kaikki 6 kokonaistuotanto-kenttää

- **Mitä testataan:** Kaikki `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto`-kentät (aurinkosahko, aurinkolampo, tuulisahko, lampopumppu, muulampo, muusahko) ovat skeemassa.
- **Assertio:** Jokaiselle 6 kentälle `flattenSchema(schema)` sisältää avaimen `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.<kentta>` ja operaattorit ovat numeerisia vertailuja (`numberComparisons`).

### 1.5 `flattenSchema` sisältää kasvihuonepäästöt per neliö -kentän

- **Mitä testataan:** Computed field kasvihuonepäästöille on mukana skeemassa numeerisena hakukenttänä.
- **Assertio:** `flattenSchema(schema)` sisältää vastaavan avaimen ja operaattorit ovat numeerisia vertailuja.

### 1.6 `flattenSchema` sisältää uusiutuvan energian osuus -kentän

- **Mitä testataan:** Stub-kenttä uusiutuvan energian osuudelle on mukana skeemassa numeerisena hakukenttänä.
- **Assertio:** `flattenSchema(schema)` sisältää vastaavan avaimen ja operaattorit ovat numeerisia vertailuja.

### 1.7 `paakayttajaSchema` sisältää uudet ET2026-kentät

- **Mitä testataan:** `paakayttajaSchema` (joka muodostetaan `flattenSchema(schema)`:sta) sisältää kaikki uudet kentät.
- **Assertio:** Kaikki edellä mainitut avaimet löytyvät `paakayttajaSchema`-objektista.

### 1.8 `laatijaSchema` ei sisällä uusia ET2026-kenttejä

- **Mitä testataan:** `laatijaSchema` rajaa näkyvät kentät omalla logiikallaan eikä uusia kenttiä lisätä laatija-hakuun.
- **Assertio:** Uudet ET2026-avaimet eivät löydy `laatijaSchema`-objektista.

---

## 2. OPERATOR_TYPES-laajennos

### Testitiedosto: `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema_test.js` (muokataan)

### 2.1 `OPERATOR_TYPES` sisältää `HAVAINNOINTIKAYNTITYYPPI`-arvon

- **Mitä testataan:** `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI` on määritelty ja on merkkijono.
- **Assertio:** `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI` on truthy ja tyypiltään string.

### 2.2 `OPERATOR_TYPES` on edelleen frozen

- **Mitä testataan:** `OPERATOR_TYPES` on `Object.freeze`-suojattu myös lisäyksen jälkeen.
- **Assertio:** `Object.isFrozen(OPERATOR_TYPES)` on `true`.

---

## 3. Havainnointikäyntityyppi-input-komponentti

### Testitiedosto: `etp-front/src/pages/energiatodistus/energiatodistus-haku/querybuilder/query-inputs/havainnointikayntityyppi-input.test.js` (uusi tiedosto)

### 3.1 Komponentti renderöityy

- **Mitä testataan:** `havainnointikayntityyppi-input.svelte` renderöityy ilman virheitä kun sille annetaan vaaditut propsit (`nameprefix`, `index`, `values`, `luokittelut`).
- **Assertio:** Komponentti mounttaa onnistuneesti eikä heittää virhettä.

### 3.2 Komponentti välittää `luokittelu='havainnointikayntityyppi'` geneeriselle `luokittelu-input.svelte`:lle

- **Mitä testataan:** Wrapper-komponentti noudattaa samaa patternia kuin `ilmanvaihtotyyppi-input.svelte` ja `lammitysmuoto-input.svelte` – se välittää `luokittelu`-propin geneeriselle komponentille.
- **Assertio:** Renderöity DOM sisältää Select-elementin, joka hyödyntää havainnointikäyntityyppien luokittelutietoja.

> **✅ Korjattu 2026-03-26.** Toteutus välitti `luokittelu='havainnointikayntityypit'` (monikko) → korjattu `'havainnointikayntityyppi'` (yksikkö).

### 3.3 Komponentti näyttää oikeat luokitteluvaihtoehdot

- **Mitä testataan:** Kun `luokittelut`-prop sisältää `havainnointikayntityyppi`-listan `[{id: 1, 'label-fi': 'Paikan päällä'}, ...]`, valintalista näyttää ne.
- **Assertio:** Select-komponentin vaihtoehdot vastaavat annettuja luokittelutietoja.

> **✅ Korjattu 2026-03-26.** Testin mock-data käytti avainta `havainnointikayntityypit` (monikko) → korjattu `'havainnointikayntityyppi'` (yksikkö) vastaamaan API:n `luokittelut`-objektin avainta.

---

## 4. Query-input-komponentti (inputForType-laajennus)

### Testitiedosto: `etp-front/src/pages/energiatodistus/energiatodistus-haku/querybuilder/query-input.test.js` (uusi tiedosto)

### 4.1 `inputForType` palauttaa `havainnointikayntityyppi-input`-komponentin tyypille `HAVAINNOINTIKAYNTITYYPPI`

- **Mitä testataan:** Switch-lausekkeen uusi case palauttaa oikean komponentti-importin.
- **Assertio:** `inputForType(OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI)` palauttaa `HavainnointikayntiTyyppiInput`-komponentin.

### 4.2 Olemassa olevat tyyppimatchiit toimivat edelleen (regressio)

- **Mitä testataan:** Aiemmat OPERATOR_TYPES-arvot (STRING, NUMBER, BOOLEAN, ILMANVAIHTOTYYPPI, LAMMITYSMUOTO jne.) palauttavat edelleen oikeat komponentit.
- **Assertio:** Jokainen olemassa oleva OPERATOR_TYPE matchaa oikeaan komponenttiin.

---

## 5. Operaattori- ja oletusarvo-testit uusille kentille

### Testitiedosto: `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema_test.js` (muokataan)

### 5.1 Havainnointikayntityyppi-kentän operaattorit

- **Mitä testataan:** `havainnointikayntityyppi-id`-kenttä käyttää `=`-operaattoria ja sen `format` tuottaa oikean serverCommand-muodon (kuten `luokitteluEquals`-pattern).
- **Assertio:** Kentän operaation `serverCommand` on `'='` ja `defaultValues()` palauttaa alkuarvon (0 tai vastaava).

### 5.2 Boolean-kenttien operaattorit

- **Mitä testataan:** `energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin` ja `lammonjako-lampotilajousto` käyttävät `singleBoolean`-patternia.
- **Assertio:** Kummankin kentän `type` on `OPERATOR_TYPES.BOOLEAN` ja `defaultValues()` palauttaa `[true]`.

### 5.3 Kokonaistuotanto-kenttien operaattorit

- **Mitä testataan:** Kaikki 6 kokonaistuotanto-kenttää käyttävät `numberComparisons`-operaattoreita (=, >, >=, <, <=).
- **Assertio:** Jokaisella kentällä on 5 operaattoria oikeilla `serverCommand`-arvoilla.

### 5.4 Kasvihuonepäästöt per neliö -kentän operaattorit

- **Mitä testataan:** Computed field käyttää `numberComparisons`-operaattoreita.
- **Assertio:** Kentällä on 5 numeerista operaattoria.

### 5.5 Uusiutuvan energian osuus -kentän operaattorit

- **Mitä testataan:** Stub-kenttä käyttää `numberComparisons`-operaattoreita.
- **Assertio:** Kentällä on 5 numeerista operaattoria.

---

## 6. Lokalisointi

### Testitiedosto: `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema_test.js` (muokataan)

### 6.1 Jokaiselle uudelle hakukentälle on lokalisointiavain fi.json:ssa

- **Mitä testataan:** Kaikki uudet hakuskeeman avaimet löytyvät lokalisointitiedostosta `fi.json`, jotta hakuehdon pudotusvalikossa näkyy käännös eikä avain.
- **Kenttien lista:**
  - `energiatodistus.perustiedot.havainnointikayntityyppi-id`
  - `energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`
  - `energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto`
  - `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko`
  - `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo`
  - `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko`
  - `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu`
  - `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo`
  - `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko`
  - Kasvihuonepäästöt per neliö
  - Uusiutuvan energian osuus
- **Assertio:** Jokainen avain löytyy `fi.json`-tiedoston `energiatodistus.haku`-polusta.

### 6.2 Jokaiselle uudelle hakukentälle on lokalisointiavain sv.json:ssa ja teksti vastaa fi-versiota suffixilla " (sv)"

- **Mitä testataan:** Sama kuin 6.1, mutta ruotsinkieliselle `sv.json`-tiedostolle. Jokaisen avaimen ruotsinkielinen teksti on identtinen suomenkielisen tekstin kanssa, mutta päättyy merkkeihin ` (sv)`. Esim. jos `fi.json`:ssa arvo on `"Aurinkosähkö, kokonaistuotanto (kWh/vuosi)"`, niin `sv.json`:ssa se on `"Aurinkosähkö, kokonaistuotanto (kWh/vuosi) (sv)"`.
- **Assertio:** Jokainen avain löytyy `sv.json`-tiedoston vastaavasta polusta ja sen arvo on `<fi-arvo> (sv)`.

---

## Katselmoinnin status (2026-03-26)

| Testi | Status | Huomio |
|-------|--------|--------|
| 1.1–1.8 Hakuskeema | ✅ Toteutettu ja läpäisee | |
| 2.1–2.2 OPERATOR_TYPES | ✅ Toteutettu ja läpäisee | |
| 3.1 Renderöinti | ✅ Toteutettu | |
| 3.2 Luokittelu-prop | ✅ Korjattu 2026-03-26 | Komponentti ja testi korjattu käyttämään `'havainnointikayntityyppi'` (yksikkö) |
| 3.3 Luokitteluvaihtoehdot | ✅ Korjattu 2026-03-26 | Mock-avain korjattu vastaamaan API:n tuotantodataa |
| 4.1–4.2 Query-input | ✅ Toteutettu ja läpäisee | |
| 5.1–5.5 Operaattorit | ✅ Toteutettu ja läpäisee | |
| 6.1–6.2 Lokalisointi | ✅ Toteutettu ja läpäisee | |
