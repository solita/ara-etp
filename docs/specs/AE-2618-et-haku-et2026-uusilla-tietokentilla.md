# AE-2618: Kirjautumispalvelun ET-haussa ET2026 uusilla tietokentillä hakeminen

## Rajaus

Tämä tiketti koskee **ainoastaan etp-front- ja etp-core-muutoksia** (kirjautuneen käyttäjän haku). Julkisen haun (etp-public) laajennos ET2026-tuella tehdään erillisenä tikettinä.

## Yhteenveto

Pääkäyttäjänä haluan hakea myös ET2026:a hakutoiminnolla, jotta löydän ne samalla tavalla kuin aiemmat todistusversiot (ET2013 ja ET2018). Haluan mahdollisesti kohdistaa haun uusiin ET2026 tietokenttiin.

## Nykytila-analyysi

### Backend (Clojure)

**Haun ydinlogiikka:** `energiatodistus_search.clj` rakentaa dynaamisen SQL-kyselyn hakuehtojen perusteella. Haku tukee `where`-lauseketta, vapaata `keyword`-hakua ja roolipohjaista näkyvyyttä (`whoami->sql`).

**Hakukenttäskeema (private):** `private-search-schema` (`energiatodistus_search.clj`, rivi 47) yhdistää skeemoja `schemas->search-schema`-funktiolla. Tällä hetkellä mukana ovat `Energiatodistus2013` ja `Energiatodistus2018`, **mutta ei `Energiatodistus2026`**.

**Hakukenttäskeema (public):** `public-search-schema` (`energiatodistus_search.clj`, rivi 70) ja `public_energiatodistus.clj` eivät sisällä ET2026-tukea. **Tämä on tämän tiketin rajauksen ulkopuolella** – julkisen haun laajennos tehdään erikseen.

**Roolinäkyvyys:** `whoami->sql`-funktiossa (`energiatodistus_search.clj`, rivi 268) julkiselle roolille on **jo lisätty** ET2026:n käsittely (versio = 2026, samat käyttötarkoitusrajaukset kuin 2018). Pääkäyttäjä- ja laatija-roolien näkyvyyssuodattimet eivät rajoita versioittain, joten ne palautavat jo ET2026-todistuksia.

**Lasketut kentät (computed fields):** `energiatodistus_search_fields.clj` sisältää laskettuja kenttiä (painotettu kulutus, neliövuosikulutus, UA-arvot). Näistä `painotettu-kulutus-sql` **tukee jo ET2026:a** (energiamuotokerroin 2026 on mukana). Neliövuosikulutus-kentät lasketaan tällä hetkellä `Energiatodistus2018`-skeemasta – ne toimivat ET2026:lle niiltä osin kuin kentät ovat yhteisiä, mutta ET2026-spesifiset uudet kentät puuttuvat.

### Frontend (Svelte/JS)

**Hakuskeema:** `energiatodistus-haku/schema.js` määrittelee hakukentät ja operaattorit. Tämä skeema EI sisällä vielä ET2026-spesifisiä kenttiä.

**Versiosuodatin:** `versio-input.svelte` näyttää jo 2026-vaihtoehdon, jos `isEtp2026Enabled(config)` on true.

**E-luokkasuodatin:** `e-luokka-input.svelte` sisältää jo A+ ja A0 luokat luokat-listassa.

**Hakutuloslistaus:** `energiatodistukset.svelte` näyttää `energiatodistus.versio`-sarakkeen, joten ET2026 näkyy jo versionumerolla hakutuloksissa.

## Muutossuunnitelma

### 1. Backend: Lisää Energiatodistus2026 private hakuskeemaan

**Sijainti:** `energiatodistus_search.clj` → `private-search-schema`

**Mitä:** Lisätään `Energiatodistus2026` skeema `schemas->search-schema`-kutsuun yhdessä `Energiatodistus2013`:n ja `Energiatodistus2018`:n rinnalle.

**Miksi:** Ilman tätä backend ei tunne ET2026-kenttiä ja palauttaa "Unknown field" -virheen, kun pääkäyttäjä yrittää hakea ET2026-spesifisillä kentillä.

**Huomioita:** `Energiatodistus2026`-skeema (`energiatodistus.clj`, rivi 449) sisältää kaikki uudet kentät (havainnointikayntityyppi-id, energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin, lammonjako-lampotilajousto, uusiutuvat-omavaraisenergiat-kokonaistuotanto jne.). `schemas->search-schema`-funktio tekee deep-mergen, joten olemassa olevat kentät säilyvät ja uudet lisätään.

> **📝 REVIEW: RATKAISTU** – Tarkistettu migraatioista: `pt$havainnointikayntityyppi_id` (v5.55) lisätään ilman NOT NULL / DEFAULT -rajoitetta → vanhoilla ET2013/ET2018-riveillä arvo on **NULL**. Vastaavasti kokonaistuotanto-sarakkeet (v5.57) ovat nullable. NULL ei matchaa yhteenkään vertailuoperaattoriin (`=`, `>`, `<`, `between`, `ilike`), joten ET2013/ET2018-todistukset rajautuvat pois kun haetaan näillä kentillä. **Poikkeus:** boolean-kentät `energiankulutuksen_valmius_reagoida_ulkoisiin_signaaleihin` ja `lammonjako_lampotilajousto` ovat `NOT NULL DEFAULT false` (v5.60) – näissä vanhat versiot eivät rajaudu pois `= false` -haulla (dokumentoitu Riskit-osiossa).

### 2. Backend: Lisää ET2026-spesifiset lasketut kentät

**Sijainti:** `energiatodistus_search_fields.clj` → `computed-fields`

**Mitä:** Lisätään kokonaistuotanto-kenttien neliövuosikulutus-laskelmat (per nettoala) hakukentiksi. ET2026:ssa on uusi `uusiutuvat-omavaraisenergiat-kokonaistuotanto`-osio, jonka kentille tarvitaan vastaavat `-neliovuosikulutus`-variantit.

**Miksi:** Tiketti vaatii, että kokonaistuotanto-kentät (aurinkosahko_kokonaistuotanto, aurinkolampo_kokonaistuotanto jne.) ovat haettavissa.

> **📝 REVIEW:** Nykyinen `per-nettoala-for-schema` tuottaa neliövuosikulutus-kenttiä `Energiatodistus2018`-skeeman perusteella. ET2026:n `uusiutuvat-omavaraisenergiat-kokonaistuotanto` on uusi avain, joka ei ole 2018-skeemassa. Tarvitaan uusi `per-nettoala-for-schema`-kutsu, joka käyttää `Energiatodistus2026`-skeemaa ja kohdistuu polkuun `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto]`. Tietokannassa sarakkeet ovat muotoa `t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkosahko` jne. (nimeäminen tehty v5.59 migraatiossa).

### 3. Backend: Kasvihuonepäästöt per neliö ja uusiutuvan energian osuus

**Sijainti:** `energiatodistus_search_fields.clj` → `computed-fields`

**Mitä:** Tiketti mainitsee kaksi laskennallista hakukenttää:
- "energian käytöstä syntyvät kasvihuonepäästöt (per neliö)"
- "paikan päällä tuotetun uusiutuvan energian osuus energiankäytöstä"

**Kasvihuonepäästöt per neliö – laskentakaava on olemassa:**

`complete_energiatodistus.clj` sisältää jo `co2-paastot-et`-funktion, joka laskee kasvihuonepäästöt `kaytettavat-energiamuodot`-kentistä käyttäen kiinteitä CO2-kertoimia:
- kaukolampo: 0.059
- sahko: 0.05
- uusiutuvat-pat: 0.027
- fossiiliset-pat: 0.306
- kaukojaahdytys: 0.014

Kaava: `summa(energiamuoto * co2-kerroin)`. Per neliö saadaan jakamalla nettoalalla. Tämä tulee toteuttaa SQL-lausekkeena computed-kenttänä hakua varten, vastaavasti kuin `per-nettoala-sql` tekee muille kentille.

> **📝 REVIEW: RATKAISTU** – `co2-paastot-et` käyttää `kaytettavat-energiamuodot`-kenttien **laskennallisia** arvoja (ei toteutuneita). Tämä on oikein – samat kentät ovat suoraan tietokannassa `energiatodistus.t$kaytettavat_energiamuodot$*`-sarakkeina. SQL-lauseke tulee olemaan: `(coalesce(energiatodistus.t$kaytettavat_energiamuodot$kaukolampo,0) * 0.059 + coalesce(energiatodistus.t$kaytettavat_energiamuodot$sahko,0) * 0.05 + coalesce(energiatodistus.t$kaytettavat_energiamuodot$uusiutuva_polttoaine,0) * 0.027 + coalesce(energiatodistus.t$kaytettavat_energiamuodot$fossiilinen_polttoaine,0) * 0.306 + coalesce(energiatodistus.t$kaytettavat_energiamuodot$kaukojaahdytys,0) * 0.014) / nullif(energiatodistus.lt$lammitetty_nettoala, 0)`. Huom: CO2-kertoimet ovat samat kaikille todistusversioille, joten tämä toimii ET2013/ET2018/ET2026:lle ilman versiokohtaista logiikkaa.

**Uusiutuvan energian osuus – tynkätoteutus (stub):**

Varsinaista laskentakaavaa ei ole vielä määritelty. `complete_energiatodistus.clj`:ssä on kommentoitu pois funktio `uusiutuvan-osuus-paastoista` ja PDF-koodissa on TODO-kommentti. Laskentakaavan toteutus tehdään erillisen lipukkeen piirissä.

**Päätös:** Tässä tiketissä toteutetaan hakukenttä tynkätoteutuksena (stub). Kenttä rekisteröidään hakuskeemaan ja frontendiin, mutta sen SQL-lauseke palauttaa toistaiseksi vakioarvon (esim. `NULL` tai `0`). Tämä mahdollistaa:
- Hakukentän näkyvyyden käyttöliittymässä valmiiksi
- Myöhemmän laskentakaavan lisäämisen ilman frontend-muutoksia
- Yhtenäisen rakenteen muiden computed-kenttien kanssa

> **📝 REVIEW: RATKAISTU** – Stub-toteutus. Varsinainen laskentakaava toteutetaan erillisessä tiketissä. Stub-kentän SQL-lauseke on vakio (esim. `NULL`), jotta haun rakenne on valmiina mutta kenttä ei vielä palauta merkityksellistä dataa.

### 4. Frontend: Lisää ET2026-spesifiset hakukentät hakuskeemaan

**Sijainti:** `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema.js`

**Mitä:** Lisätään uudet hakukenttäoperaatiot schema-rakenteeseen:

- `perustiedot.havainnointikayntityyppi-id` – tämä on luokittelukenttä (dropdown). Vaatii uuden OPERATOR_TYPES-arvon (`HAVAINNOINTIKAYNTITYYPPI`) tai voidaan käyttää yleistä `luokitteluEquals`-operaattoria, jos havainnointikäyntityypit noudattavat samaa `{id, label-fi, label-sv}` -rakennetta kuin muut luokittelut.
- `lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin` – boolean
- `lahtotiedot.lammitys.lammonjako-lampotilajousto` – boolean
- `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko` jne. – numeerinen
- Mahdolliset lasketut kentät (kasvihuonepäästöt, uusiutuvan energian osuus)

**Miksi:** Ilman näitä käyttöliittymä ei tarjoa mahdollisuutta hakea uusilla kentillä.

> **📝 REVIEW: RATKAISTU** – Havainnointikäyntityyppi-luokittelu **on jo käytössä** ET2026-lomakkeessa (`ET2026Form.svelte`, rivi 153–168) ja **on mukana** `luokittelutAllVersions`-API:n vastauksessa (`energiatodistus-api.js`, rivi 361: `havainnointikayntityyppi: Fetch.cached(fetch, '/havainnointikayntityyppi')`). Se noudattaa samaa `{id, label-fi, label-sv}` -rakennetta kuin muut luokittelut. **Toteutusratkaisu:** Tarvitaan uusi `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI` ja ohut wrapper-komponentti (`havainnointikayntityyppi-input.svelte`), joka välittää `luokittelu={'havainnointikayntityyppi'}` geneeriselle `luokittelu-input.svelte`:lle – täsmälleen samalla patternilla kuin `ilmanvaihtotyyppi-input.svelte` ja `lammitysmuoto-input.svelte` toimivat.

### 5. Frontend: laatijaSchema – ei muutoksia tässä tiketissä

**Päätös:** Laatijan hakutoiminnallisuuden laajentamisesta on oma lipukkeensa. Tässä tiketissä uudet ET2026-hakukentät lisätään **ainoastaan pääkäyttäjäskeemaan** (`paakayttajaSchema`). `laatijaSchema` säilyy ennallaan – se ei vaadi muutoksia tässä tiketissä.

> **📝 REVIEW: RATKAISTU** – Ei muutoksia `laatijaSchema`:an. Uudet kentät lisätään `schema`-objektiin, josta `paakayttajaSchema` muodostetaan `flattenSchema(schema)`:lla. Koska `laatijaSchema` tekee omat rajoituksensa (esim. `R.pick(['e-luku', 'e-luokka'])` tuloksille), uudet kentät eivät automaattisesti näy laatijalle.

### 6. Taaksepäin yhteensopivuus

**Mitä varmistettava:**
- Kun haetaan kentällä, joka on vain ET2026:ssa (esim. `havainnointikayntityyppi-id`), ET2013/ET2018-todistukset eivät matchaa (koska arvo on NULL), eli ne rajautuvat pois – tämä on hyväksymiskriteerien mukaista.
- Kun haetaan vain ET2013/ET2018-kentillä, ET2026 rajautuu pois tuloksista vain jos kyseinen kenttä puuttuu ET2026:sta. ET2026-todistukset sisältävät suurimman osan vanhoista kentistä (koska `EnergiatodistusSave2026` perustuu `EnergiatodistusSave2018`:aan), joten ne yleensä matchaavat.
- Versio-suodatin (`energiatodistus.versio`) sallii jo 2026-arvon backend-tasolla.

> **📝 REVIEW: RATKAISTU** – `dissoc-not-in-2026` poistaa `polttoaineet-vuosikulutus-yhteensa`-kentän ET2026-skeemasta. Tämä kenttä kuitenkin **säilyy** tietokannassa (se on edelleen `toteutunut-ostoenergiankulutus`:n sarake), mutta sitä ei täytetä ET2026-todistuksille. Kun pääkäyttäjä hakee tällä kentällä, ET2026-todistukset rajautuvat pois (NULL tietokannassa). Tämä on hyväksymiskriteerien mukaista: *"Jos haussa käytetään vain vanhoihin versioihin kuuluvia kenttiä, ET2026 ei aiheuta virheitä vaan rajautuu pois tuloksista."*

### 7. E-luokka-hakuehto: A+ ja A0 tuki

**Nykytila:** E-luokka-hakukomponentti (`e-luokka-input.svelte`) listaa jo kaikki luokat mukaan lukien A+ ja A0. Backend-tasolla `e-luokka`-kenttä on yksinkertainen string-kenttä, joten A+ ja A0 toimivat sellaisenaan `in`-operaattorilla.

**Mitä varmistettava:** URL-enkoodaus – A+-merkin `+`-merkki URL:ssa. Nykyisessä koodissa (`energiatodistus-haku.svelte`, rivi 65) on jo käsittely: `s => s.replace(/\+/g, '%2B')`, joka estää `+`-merkin tulkinnan välilyöntinä.

> **📝 REVIEW: RATKAISTU** – A+/A0 e-luokkahaku on jo kunnossa. `e-luokka-input.svelte` listaa A+ ja A0, URL-enkoodaus (`%2B`) on käsitelty `energiatodistus-haku.svelte`:ssä, ja backend käsittelee `in`-operaattorilla string-arrayta. Varmistettava testeillä.

## Yhteenveto muutettavista tiedostoista

### Backend
| Tiedosto | Muutos |
|---|---|
| `energiatodistus_search.clj` | Lisää `Energiatodistus2026` private-hakuskeemaan (`private-search-schema`) |
| `energiatodistus_search_fields.clj` | Lisää kokonaistuotanto-kenttien neliövuosikulutukset, kasvihuonepäästöt per neliö (lasketut kentät) |

### Frontend
| Tiedosto | Muutos |
|---|---|
| `energiatodistus-haku/schema.js` | Lisää ET2026-spesifiset hakukentät ja `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI` |
| `querybuilder/query-inputs/havainnointikayntityyppi-input.svelte` | Uusi wrapper-komponentti (samalla patternilla kuin `ilmanvaihtotyyppi-input.svelte`) |
| `querybuilder/query-input.svelte` | Lisää `HAVAINNOINTIKAYNTITYYPPI`-case `inputForType`-switchiin |
| Lokalisointitiedostot | Uusien kenttien käännökset (fi/sv) |

## Riskit

- **Uusiutuvan energian osuus toteutetaan stub-kenttänä.** Hakukenttä on olemassa mutta palauttaa vakioarvon. Varsinainen laskentakaava toteutetaan erillisessä tiketissä. Riski: käyttäjä voi hämmentyä kentästä, joka ei tuota tuloksia.
- **Deep-merge yhdistäminen voi tuottaa yllättäviä tuloksia**, jos ET2026-skeema määrittelee saman polun eri tyypillä kuin ET2018. Tämä on epätodennäköistä nykyisellä skeemarakenteella, mutta on hyvä testata.
- **Boolean-kenttien oletusarvot tietokannassa:** Migraatio `v5.60` asettaa `energiankulutuksen_valmius_reagoida_ulkoisiin_signaaleihin` ja `lammonjako_lampotilajousto` oletusarvoksi `false` (NOT NULL). Tämä tarkoittaa, että ET2013/ET2018-riveillä näiden arvo on `false`, ei NULL. Hakuehto `= true` toimii oikein (rajaa pois vanhat versiot), mutta `= false` palauttaa **myös vanhat versiot**. Tämä voi olla yllättävää – harkittava, halutaanko rajoittaa näitä hakukenttiä vain ET2026-versioon.
