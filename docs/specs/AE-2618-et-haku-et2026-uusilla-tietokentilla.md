# AE-2618: Kirjautumispalvelun ET-haussa ET2026 uusilla tietokentillä hakeminen

## Yhteenveto

Pääkäyttäjänä haluan hakea myös ET2026:a hakutoiminnolla, jotta löydän ne samalla tavalla kuin aiemmat todistusversiot (ET2013 ja ET2018). Haluan mahdollisesti kohdistaa haun uusiin ET2026 tietokenttiin.

## Nykytila-analyysi

### Backend (Clojure)

**Haun ydinlogiikka:** `energiatodistus_search.clj` rakentaa dynaamisen SQL-kyselyn hakuehtojen perusteella. Haku tukee `where`-lauseketta, vapaata `keyword`-hakua ja roolipohjaista näkyvyyttä (`whoami->sql`).

**Hakukenttäskeema (private):** `private-search-schema` (`energiatodistus_search.clj`, rivi 47) yhdistää skeemoja `schemas->search-schema`-funktiolla. Tällä hetkellä mukana ovat `Energiatodistus2013` ja `Energiatodistus2018`, **mutta ei `Energiatodistus2026`**.

**Hakukenttäskeema (public):** `public-search-schema` (`energiatodistus_search.clj`, rivi 70) yhdistää vastaavasti `Energiatodistus2013` ja `Energiatodistus2018`, **mutta ei `Energiatodistus2026`**. Lisäksi `public_energiatodistus.clj`:n `Energiatodistus`-conditional-skeema ei sisällä ET2026:a.

**Roolinäkyvyys:** `whoami->sql`-funktiossa (`energiatodistus_search.clj`, rivi 268) julkiselle roolille on **jo lisätty** ET2026:n käsittely (versio = 2026, samat käyttötarkoitusrajaukset kuin 2018).

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

> **📝 REVIEW:** Tarkista, mitä tapahtuu kun `Energiatodistus2026`-skeemassa on kenttä (esim. `havainnointikayntityyppi-id`), joka lisätään `deep-merge`-yhdistykseen, mutta jota ei löydy tietokannasta ET2013/ET2018-riveiltä. Oletettavasti nämä ovat NULL tietokannassa vanhemmille versioille, jolloin haku toimii oikein (NULL ei matchaa mihinkään vertailuun). Tämä kannattaa varmistaa testeillä.

### 2. Backend: Lisää Energiatodistus2026 public hakuskeemaan

**Sijainti:** `energiatodistus_search.clj` → `public-search-schema` ja `public_energiatodistus.clj`

**Mitä:**
- Lisätään `public-search-schema`-kutsuun ET2026 public-skeema
- Lisätään `public_energiatodistus.clj`:n `Energiatodistus`-conditional-skeemaan ET2026-haara

**Miksi:** Julkinen haku ei palauta ET2026-todistuksia eikä tunnista niiden kenttiä ilman tätä muutosta.

> **📝 REVIEW:** `public_energiatodistus.clj`:stä puuttuu kokonaan `Energiatodistus2026`-määritys. Tarvitaan uusi `(def Energiatodistus2026 ...)` ja se pitää lisätä `Energiatodistus`-conditional-skeemaan. Mietittävä, mitkä ET2026-kentät näytetään julkisesti – todennäköisesti samat peruskentät kuin 2018:lle, plus mahdollisesti joitain uusia (esim. e-luokka A+/A0 näkyy jo). Vaatiiko tämä tuoteomistajalta päätöksen julkisista kentistä?

### 3. Backend: Lisää ET2026-spesifiset lasketut kentät

**Sijainti:** `energiatodistus_search_fields.clj` → `computed-fields`

**Mitä:** Lisätään kokonaistuotanto-kenttien neliövuosikulutus-laskelmat (per nettoala) hakukentiksi. ET2026:ssa on uusi `uusiutuvat-omavaraisenergiat-kokonaistuotanto`-osio, jonka kentille tarvitaan vastaavat `-neliovuosikulutus`-variantit.

**Miksi:** Tiketti vaatii, että kokonaistuotanto-kentät (aurinkosahko_kokonaistuotanto, aurinkolampo_kokonaistuotanto jne.) ovat haettavissa.

> **📝 REVIEW:** Nykyinen `per-nettoala-for-schema` tuottaa neliövuosikulutus-kenttiä `Energiatodistus2018`-skeeman perusteella. ET2026:n `uusiutuvat-omavaraisenergiat-kokonaistuotanto` on uusi avain, joka ei ole 2018-skeemassa. Tarvitaan uusi `per-nettoala-for-schema`-kutsu, joka käyttää `Energiatodistus2026`-skeemaa ja kohdistuu polkuun `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto]`.

### 4. Backend: Kasvihuonepäästöt per neliö ja uusiutuvan energian osuus

**Sijainti:** `energiatodistus_search_fields.clj` → `computed-fields`

**Mitä:** Tiketti mainitsee kaksi laskennallista hakukenttää:
- "energian käytöstä syntyvät kasvihuonepäästöt (per neliö)"
- "paikan päällä tuotetun uusiutuvan energian osuus energiankäytöstä"

Nämä ovat laskettuja kenttiä, joita ei välttämättä ole suoraan tietokannassa, vaan ne pitää johtaa olemassa olevista sarakkeista.

> **📝 REVIEW: KRIITTINEN** – Nämä kaksi kenttää vaativat tarkempaa selvitystä:
>
> 1. **Kasvihuonepäästöt per neliö:** Onko tämä arvo tallennettuna tietokantaan vai lasketaanko se? Mitkä sarakkeet ovat lähtödatana? Tarvitaanko päästökertoimet tietokantaan tai konfiguraatioon?
> 2. **Uusiutuvan energian osuus:** Miten tämä lasketaan? Onko kaava = (uusiutuvat omavaraisenergiat yhteensä) / (kokonaisenergiankulutus)? Mistä kentistä nämä muodostuvat?
>
> Ilman näitä vastauksia ei voida suunnitella SQL-lausekkeita. **Tarvitaan tiketinhaltijan/tuoteomistajan tarkennusta.**

### 5. Frontend: Lisää ET2026-spesifiset hakukentät hakuskeemaan

**Sijainti:** `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema.js`

**Mitä:** Lisätään uudet hakukenttäoperaatiot schema-rakenteeseen:

- `perustiedot.havainnointikayntityyppi-id` – tämä on luokittelukenttä (dropdown). Vaatii uuden OPERATOR_TYPES-arvon (`HAVAINNOINTIKAYNTITYYPPI`) tai voidaan käyttää yleistä `luokitteluEquals`-operaattoria, jos havainnointikäyntityypit noudattavat samaa `{id, label-fi, label-sv}` -rakennetta kuin muut luokittelut.
- `lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin` – boolean
- `lahtotiedot.lammitys.lammonjako-lampotilajousto` – boolean
- `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko` jne. – numeerinen
- Mahdolliset lasketut kentät (kasvihuonepäästöt, uusiutuvan energian osuus)

**Miksi:** Ilman näitä käyttöliittymä ei tarjoa mahdollisuutta hakea uusilla kentillä.

> **📝 REVIEW:** Tarkista, tarvitaanko uusi input-komponentti `havainnointikayntityyppi-input.svelte` vai voidaanko käyttää olemassa olevaa `luokittelu-input.svelte`-komponenttia. Luokitteluarvot haetaan `luokittelutAllVersions`-API-kutsulla – varmista, että havainnointikäyntityypit ovat mukana.

### 6. Frontend: Varmista, ettei laatijaSchema rajoita uusia kenttiä

**Sijainti:** `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema.js` → `laatijaSchema`

**Mitä:** Tarkista, ovatko uudet kentät oleellisia myös laatijan hakuskeemassa vai vain pääkäyttäjän skeemassa. Nykyisessä `laatijaSchema`-määrittelyssä tulokset rajoitetaan vain `e-luku`- ja `e-luokka`-kenttiin. Jos laatijan halutaan voivan hakea kokonaistuotanto-kentillä, `laatijaSchema`:n `tulokset`-pick-listaa pitää laajentaa.

> **📝 REVIEW:** Tarvitaanko tuoteomistajan päätös siitä, mitkä uudet kentät ovat laatijan käytettävissä? Nykyinen rajoitus (`laatijaSchema` rajoittaa tulokset vain `e-luku`/`e-luokka`-kenttiin) viittaa siihen, että laatijalle näytetään vähemmän hakuvaihtoehtoja. Todennäköisesti uudet kentät tarvitaan vain pääkäyttäjäskeemaan.

### 7. Taaksepäin yhteensopivuus

**Mitä varmistettava:**
- Kun haetaan kentällä, joka on vain ET2026:ssa (esim. `havainnointikayntityyppi-id`), ET2013/ET2018-todistukset eivät matchaa (koska arvo on NULL), eli ne rajautuvat pois – tämä on hyväksymiskriteerien mukaista.
- Kun haetaan vain ET2013/ET2018-kentillä, ET2026 rajautuu pois tuloksista vain jos kyseinen kenttä puuttuu ET2026:sta. ET2026-todistukset sisältävät suurimman osan vanhoista kentistä (koska `EnergiatodistusSave2026` perustuu `EnergiatodistusSave2018`:aan), joten ne yleensä matchaavat.
- Versio-suodatin (`energiatodistus.versio`) sallii jo 2026-arvon backend-tasolla.

> **📝 REVIEW:** Tässä on tärkeä edge case: `EnergiatodistusSave2026` käyttää `dissoc-not-in-2026`-funktiota, joka poistaa `polttoaineet-vuosikulutus-yhteensa`-kentän. Jos pääkäyttäjä hakee tällä kentällä, ET2026-todistukset rajautuvat pois (NULL). Tämä on todennäköisesti ok, mutta dokumentoidaan.

### 8. E-luokka-hakuehto: A+ ja A0 tuki

**Nykytila:** E-luokka-hakukomponentti (`e-luokka-input.svelte`) listaa jo kaikki luokat mukaan lukien A+ ja A0. Backend-tasolla `e-luokka`-kenttä on yksinkertainen string-kenttä, joten A+ ja A0 toimivat sellaisenaan `in`-operaattorilla.

**Mitä varmistettava:** URL-enkoodaus – A+-merkin `+`-merkki URL:ssa. Nykyisessä koodissa (`energiatodistus-haku.svelte`, rivi 65) on jo käsittely: `s => s.replace(/\+/g, '%2B')`, joka estää `+`-merkin tulkinnan välilyöntinä.

> **📝 REVIEW:** Tämä vaikuttaa olevan jo kunnossa. Testeillä varmistettava, että A+-haku toimii oikein koko ketjussa (URL → backend → SQL → tulokset).

## Yhteenveto muutettavista tiedostoista

### Backend
| Tiedosto | Muutos |
|---|---|
| `energiatodistus_search.clj` | Lisää `Energiatodistus2026` private- ja public-hakuskeemoihin |
| `energiatodistus_search_fields.clj` | Lisää kokonaistuotanto-kentät, kasvihuonepäästöt per neliö, uusiutuvan energian osuus (lasketut kentät) |
| `public_energiatodistus.clj` | Lisää `Energiatodistus2026`-skeema ja liitä conditional-skeemaan |

### Frontend
| Tiedosto | Muutos |
|---|---|
| `energiatodistus-haku/schema.js` | Lisää ET2026-spesifiset hakukentät (havainnointikayntityyppi, boolean-kentät, kokonaistuotanto jne.) |
| Mahdollisesti uusi input-komponentti | `havainnointikayntityyppi-input.svelte` tai käytetään olemassa olevaa |
| Mahdollisesti lokalisointitiedostot | Uusien kenttien käännökset |

## Avoimet kysymykset

1. **Kasvihuonepäästöt per neliö:** Onko tämä tallennettu tietokantaan vai laskettava kenttä? Mikä on laskentakaava?
2. **Uusiutuvan energian osuus:** Sama kysymys – mistä kentistä ja millä kaavalla?
3. **Julkisen haun ET2026-kentät:** Mitkä ET2026-spesifiset kentät näkyvät julkisessa haussa?
4. **Laatijan hakukentät:** Tarvitseeko laatija hakea uusilla ET2026-kentillä vai riittääkö pääkäyttäjäskeema?
5. **Havainnointikäyntityyppi-luokittelu:** Sisältyykö tämä jo `luokittelutAllVersions`-API:n vastaukseen?

## Riskit

- **Kasvihuonepäästöt/uusiutuvan osuus -kenttien laskentakaavat puuttuvat.** Nämä voivat vaatia tuoteomistajan tarkennusta ennen toteutusta.
- **Deep-merge yhdistäminen voi tuottaa yllättäviä tuloksia**, jos ET2026-skeema määrittelee saman polun eri tyypillä kuin ET2018. Tämä on epätodennäköistä nykyisellä skeemarakenteella, mutta on hyvä testata.
- **Public-skeeman puuttuminen** voi aiheuttaa ongelmia julkiselle hakupalvelulle, jos sitä ei toteuteta samalla.
