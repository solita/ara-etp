# AE-2618: Kirjautumispalvelun ET-haussa ET2026 uusilla tietokentillä hakeminen

## Rajaus

Tämä tiketti koskee **ainoastaan etp-front- ja etp-core-muutoksia** (kirjautuneen pääkäyttäjän haku). Julkisen haun (etp-public) laajennos ET2026-tuella ja laatijan hakutoiminnallisuuden laajentaminen tehdään erillisinä tiketteinä.

## Yhteenveto

Pääkäyttäjänä haluan hakea myös ET2026:a hakutoiminnolla, jotta löydän ne samalla tavalla kuin aiemmat todistusversiot (ET2013 ja ET2018). Haluan mahdollisesti kohdistaa haun uusiin ET2026 tietokenttiin.

## Nykytila-analyysi

### Backend (Clojure)

**Haun ydinlogiikka:** `energiatodistus_search.clj` rakentaa dynaamisen SQL-kyselyn hakuehtojen perusteella. Haku tukee `where`-lauseketta, vapaata `keyword`-hakua ja roolipohjaista näkyvyyttä (`whoami->sql`).

**Hakukenttäskeema (private):** `private-search-schema` (`energiatodistus_search.clj`) yhdistää skeemoja `schemas->search-schema`-funktiolla. Tällä hetkellä mukana ovat `Energiatodistus2013` ja `Energiatodistus2018`, mutta ei `Energiatodistus2026`.

**Roolinäkyvyys:** `whoami->sql`-funktiossa julkiselle roolille on jo lisätty ET2026:n käsittely. Pääkäyttäjä- ja laatija-roolien näkyvyyssuodattimet eivät rajoita versioittain, joten ne palautavat jo ET2026-todistuksia.

**Lasketut kentät (computed fields):** `energiatodistus_search_fields.clj` sisältää laskettuja kenttiä (painotettu kulutus, neliövuosikulutus, UA-arvot). Näistä `painotettu-kulutus-sql` tukee jo ET2026:a (energiamuotokerroin 2026 on mukana). Neliövuosikulutus-kentät lasketaan `Energiatodistus2018`-skeemasta – ne toimivat ET2026:lle niiltä osin kuin kentät ovat yhteisiä, mutta ET2026-spesifiset uudet kentät puuttuvat.

### Frontend (Svelte/JS)

**Hakuskeema:** `energiatodistus-haku/schema.js` määrittelee hakukentät ja operaattorit. Skeema ei sisällä vielä ET2026-spesifisiä kenttiä.

**Versiosuodatin:** `versio-input.svelte` näyttää jo 2026-vaihtoehdon, kun `isEtp2026Enabled(config)` on true.

**E-luokkasuodatin:** `e-luokka-input.svelte` sisältää jo A+ ja A0 luokat. URL-enkoodaus `+`-merkille on käsitelty (`energiatodistus-haku.svelte`: `s => s.replace(/\+/g, '%2B')`).

**Hakutuloslistaus:** `energiatodistukset.svelte` näyttää `energiatodistus.versio`-sarakkeen, joten ET2026 näkyy jo versionumerolla hakutuloksissa.

## Muutossuunnitelma

### 1. Backend: Lisää Energiatodistus2026 private-hakuskeemaan

**Sijainti:** `energiatodistus_search.clj` → `private-search-schema`

**Mitä:** Lisätään `Energiatodistus2026`-skeema `schemas->search-schema`-kutsuun `Energiatodistus2013`:n ja `Energiatodistus2018`:n rinnalle.

**Miksi:** Ilman tätä backend ei tunne ET2026-kenttiä ja palauttaa "Unknown field" -virheen, kun pääkäyttäjä yrittää hakea ET2026-spesifisillä kentillä.

**Huomioita:** `Energiatodistus2026`-skeema (`energiatodistus.clj`) sisältää kaikki uudet kentät. `schemas->search-schema`-funktio tekee deep-mergen, joten olemassa olevat kentät säilyvät ja uudet lisätään.

**Vaikutus vanhoihin versioihin:** ET2026-spesifiset sarakkeet (esim. `pt$havainnointikayntityyppi_id`, kokonaistuotanto-sarakkeet) ovat nullable tietokannassa – vanhoilla ET2013/ET2018-riveillä arvo on NULL. NULL ei matchaa vertailuoperaattoreihin, joten vanhat todistukset rajautuvat pois kun haetaan näillä kentillä. Poikkeus: boolean-kentät `energiankulutuksen_valmius_reagoida_ulkoisiin_signaaleihin` ja `lammonjako_lampotilajousto` ovat `NOT NULL DEFAULT false` (v5.60-migraatio) – näissä myös vanhojen versioiden arvo on `false` (ks. Riskit-osio).

### 2. Backend: Lisää ET2026-spesifiset lasketut kentät

**Sijainti:** `energiatodistus_search_fields.clj` → `computed-fields`

**Mitä:** Lisätään kokonaistuotanto-kenttien neliövuosikulutus-laskelmat (per nettoala) hakukentiksi. ET2026:ssa on uusi `uusiutuvat-omavaraisenergiat-kokonaistuotanto`-osio, jonka kentille tarvitaan vastaavat `-neliovuosikulutus`-variantit.

**Miksi:** Tiketti vaatii, että kokonaistuotanto-kentät (aurinkosahko, aurinkolampo, tuulisahko, lampopumppu, muulampo, muusahko) ovat haettavissa.

**Toteutustapa:** Uusi `per-nettoala-for-schema`-kutsu, joka käyttää `Energiatodistus2026`-skeemaa ja kohdistuu polkuun `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto]`. Tietokannassa sarakkeet ovat muotoa `t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkosahko` jne. (v5.59-migraatio).

### 3. Backend: Kasvihuonepäästöt per neliö ja uusiutuvan energian osuus

**Sijainti:** `energiatodistus_search_fields.clj` → `computed-fields`

**Kasvihuonepäästöt per neliö:**

Lasketaan `kaytettavat-energiamuodot`-kenttien laskennallisista arvoista käyttäen kiinteitä CO2-kertoimia (samat kaikille todistusversioille):

| Energiamuoto | CO2-kerroin |
|---|---|
| kaukolampo | 0.059 |
| sahko | 0.05 |
| uusiutuva-polttoaine | 0.027 |
| fossiilinen-polttoaine | 0.306 |
| kaukojaahdytys | 0.014 |

Kaava: `summa(energiamuoto * co2-kerroin) / nettoala`. Toteutetaan SQL computed field -lausekkeena, joka käyttää `coalesce`-funktiota NULL-arvojen käsittelyyn ja `nullif`-funktiota nollalla jakamisen estämiseksi. Kertoimet ovat samat kaikille todistusversioille, joten versiokohtaista logiikkaa ei tarvita.

**Uusiutuvan energian osuus – tynkätoteutus (stub):**

Varsinaista laskentakaavaa ei ole vielä määritelty – se toteutetaan erillisen lipukkeen piirissä. Tässä tiketissä rekisteröidään hakukenttä tynkätoteutuksena: kenttä lisätään hakuskeemaan ja frontendiin, mutta SQL-lauseke palauttaa toistaiseksi vakioarvon `0`. Tämä mahdollistaa hakukentän rakenteen valmiiksi ja myöhemmän laskentakaavan lisäämisen ilman frontend-muutoksia.

### 4. Frontend: Lisää ET2026-spesifiset hakukentät pääkäyttäjän hakuskeemaan

**Sijainti:** `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema.js`

**Mitä:** Lisätään uudet hakukenttäoperaatiot `schema`-rakenteeseen (josta `paakayttajaSchema` muodostetaan `flattenSchema(schema)`:lla). `laatijaSchema`:an ei tehdä muutoksia – se rajaa näkyvät kentät omalla logiikallaan, eivätkä uudet kentät näy laatijalle.

Uudet hakukentät:

- **`perustiedot.havainnointikayntityyppi-id`** – luokittelukenttä (dropdown). Havainnointikäyntityypit ovat mukana `luokittelutAllVersions`-API:n vastauksessa (`energiatodistus-api.js`: `havainnointikayntityyppi: Fetch.cached(fetch, '/havainnointikayntityyppi')`) ja noudattavat samaa `{id, label-fi, label-sv}` -rakennetta kuin muut luokittelut. Toteutetaan uudella `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI`-arvolla ja wrapper-komponentilla `havainnointikayntityyppi-input.svelte`, joka välittää `luokittelu={'havainnointikayntityyppi'}` geneeriselle `luokittelu-input.svelte`:lle – samalla patternilla kuin `ilmanvaihtotyyppi-input.svelte` ja `lammitysmuoto-input.svelte`.
- **`lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`** – boolean (`singleBoolean`)
- **`lahtotiedot.lammitys.lammonjako-lampotilajousto`** – boolean (`singleBoolean`)
- **`tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko`** jne. – numeerinen (`numberComparisons`), kaikki 6 kokonaistuotanto-kenttää
- **Kasvihuonepäästöt per neliö** – numeerinen computed field (`numberComparisons`)
- **Uusiutuvan energian osuus** – numeerinen computed field (`numberComparisons`), stub-toteutus

### 5. Taaksepäin yhteensopivuus

Seuraavat asiat on varmistettu:

- ET2026-spesifisillä kentillä (esim. `havainnointikayntityyppi-id`) haettaessa ET2013/ET2018-todistukset rajautuvat pois, koska näiden sarakkeiden arvo on NULL vanhoilla riveillä. Tämä on hyväksymiskriteerien mukaista.
- ET2013/ET2018-kentillä haettaessa ET2026-todistukset yleensä matchaavat, koska `EnergiatodistusSave2026` perustuu `EnergiatodistusSave2018`:aan ja sisältää suurimman osan samoista kentistä. Poikkeus: `polttoaineet-vuosikulutus-yhteensa` on poistettu ET2026-skeemasta (`dissoc-not-in-2026`), joten tällä kentällä haettaessa ET2026-todistukset rajautuvat pois (NULL tietokannassa). Tämä on hyväksymiskriteerien mukaista.
- Versio-suodatin (`energiatodistus.versio`) sallii jo 2026-arvon backend-tasolla.
- E-luokkahaku A+/A0-arvoilla toimii jo: `e-luokka-input.svelte` listaa ne, URL-enkoodaus on käsitelty, ja backend tukee `in`-operaattoria string-arraylle.

## Yhteenveto muutettavista tiedostoista

### Backend

| Tiedosto | Muutos |
|---|---|
| `energiatodistus_search.clj` | Lisää `Energiatodistus2026` `private-search-schema`:an |
| `energiatodistus_search_fields.clj` | Lisää kokonaistuotanto-kenttien neliövuosikulutukset, kasvihuonepäästöt per neliö, uusiutuvan energian osuus (stub) |

### Frontend

| Tiedosto | Muutos |
|---|---|
| `energiatodistus-haku/schema.js` | Lisää ET2026-hakukentät ja `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI` |
| `querybuilder/query-inputs/havainnointikayntityyppi-input.svelte` | Uusi wrapper-komponentti |
| `querybuilder/query-input.svelte` | Lisää `HAVAINNOINTIKAYNTITYYPPI`-case `inputForType`-switchiin |
| Lokalisointitiedostot | Uusien kenttien käännökset (fi/sv) |

## Riskit

- **Uusiutuvan energian osuus toteutetaan stub-kenttänä.** Hakukenttä on olemassa mutta palauttaa vakioarvon `0`. Varsinainen laskentakaava toteutetaan erillisessä tiketissä. Käyttäjä voi hämmentyä kentästä, joka ei vielä tuota todellisia arvoja.
- **Deep-merge voi tuottaa yllättäviä tuloksia**, jos ET2026-skeema määrittelee saman polun eri tyypillä kuin ET2018. Tämä on epätodennäköistä nykyisellä skeemarakenteella, mutta on hyvä testata.
- **Boolean-kenttien oletusarvot tietokannassa:** `energiankulutuksen_valmius_reagoida_ulkoisiin_signaaleihin` ja `lammonjako_lampotilajousto` ovat `NOT NULL DEFAULT false` (v5.60-migraatio), joten ET2013/ET2018-riveillä arvo on `false`, ei NULL. Hakuehto `= true` rajaa oikein vanhat versiot pois, mutta `= false` palauttaa myös vanhat versiot.

---

## Toteutuksen status (katselmoitu 2026-03-26)

### Backend – Status: ✅ Valmis

| Osa | Status | Huomio |
|-----|--------|--------|
| 1. Energiatodistus2026 private-search-schema:an | ✅ Valmis | |
| 2. Kokonaistuotanto neliövuosikulutus computed fields | ✅ Valmis | |
| 3. Kasvihuonepäästöt per neliö | ✅ Valmis | coalesce/nullif käsittely oikein |
| 3. Uusiutuvan energian osuus (stub) | ✅ Valmis | Palauttaa "0" |

### Frontend – Status: ✅ Valmis

| Osa | Status | Huomio |
|-----|--------|--------|
| 4. OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI | ✅ Valmis | |
| 4. Hakukentät schema.js:ään | ✅ Valmis | |
| 4. havainnointikayntityyppi-input.svelte | ✅ Valmis | Korjattu 2026-03-26: `luokittelu='havainnointikayntityypit'` → `'havainnointikayntityyppi'` (yksikkö) |
| 4. query-input.svelte case | ✅ Valmis | |
| 4. Lokalisointi (fi/sv) | ✅ Valmis | sv-käännökset placeholder-muodossa |
| 5. Taaksepäin yhteensopivuus | ✅ Valmis | laatijaSchema rajaa uudet kentät pois |

### Katselmoinnin löydökset

1. **~~🔴 KRIITTINEN:~~** ✅ **Korjattu 2026-03-26.** `havainnointikayntityyppi-input.svelte` välitti väärän luokittelu-avaimen `'havainnointikayntityypit'` (monikko) → korjattu `'havainnointikayntityyppi'` (yksikkö). Myös testin mock-avain korjattu vastaamaan API:n avainta.
2. **🟡 Huomio:** CO₂-kertoimet (0.059, 0.05 jne.) kovakoodattu sekä SQL:ään että testeihin – kaksi päivityskohdetta jos kertoimet muuttuvat.
3. **🟡 Huomio:** 6 kokonaistuotanto-testiä voisi parametrisoida (ei estävä).
