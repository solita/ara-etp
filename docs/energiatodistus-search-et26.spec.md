# Hakuehtojen mukaisten laatimieni energiatodistusten lataus .csv -tiedostona (uudet tietokentät, ET2026)

## Käyttäjätarina

Laatijana haluan ladata .csv-tiedostona kaikki hakuehtojeni mukaiset itse laatimani energiatodistukset, myös ET2026, jotta voin käsitellä niitä omissa työkaluissani.

## Hyväksymiskriteerit

1. Pystyn hakemaan uusilla tietokentillä (laatijalla vähemmän hakuehtoja kuin pääkäyttäjällä).
2. Lataus sisältää vain käyttäjän itse laatimat todistukset ja vain kulloinkin voimassa olevat hakuehdot (esim. aikarajaus, tila, energialuokka).
3. CSV-lataus sisältää ET2026-version uudet tietokentät sarakkeinaan, kun ladattavissa todistuksissa on versio 2026.

---

## Nykytilan kuvaus

### Backend: Haku ja autorisointi

Hakupalvelu `energiatodistus-search` rajaa tuloksia `whoami`-kontekstin perusteella (ks. `whoami->sql`-funktio tiedostossa `energiatodistus_search.clj`):

- **Laatija** näkee vain omat todistuksensa (`energiatodistus.laatija_id = ?`) ja vain tilat `0, 1, 2, 3, 4, 7`.
- **Pääkäyttäjä** näkee tilat `2, 3, 4, 6, 7` sekä luonnokset joissa `draft_visible_to_paakayttaja = true`.

Tämä rajaus on jo olemassa ja toimii automaattisesti myös ET2026-todistuksille. **Tässä ei tarvita muutoksia.**

### Backend: Hakukentät (search schema)

`private-search-schema` (tiedostossa `energiatodistus_search.clj`) koostaan yhdistämällä `Energiatodistus2013` ja `Energiatodistus2018` -skeemoja, mutta **`Energiatodistus2026`-skeemaa ei ole lisätty** hakukenttiin. Tämä tarkoittaa, etteivät ET2026-spesifiset kentät (esim. `havainnointikayntityyppi-id`, `kasvihuonepaastojen-muutos`, `ilmastoselvitys`-kentät ym.) ole käytettävissä hakuehtoina.

**Tarvittava muutos:** `Energiatodistus2026` tulee lisätä `private-search-schema`-konstruktioon (ja vastaavasti `public-search-schema`-konstruktioon niiltä osin kuin julkinen skeema edellyttää).

### Backend: CSV-sarakkeet

CSV-palvelu (`energiatodistus_csv.clj`) määrittelee useita sarakelistoja eri käyttötarkoituksiin. Tämän featuren kannalta oleellinen on `private-columns`, jota käytetään yksityisen API:n CSV-latauksessa (`energiatodistukset-private-csv`, reitti `/api/private/.../csv/...`). Laatijan ja pääkäyttäjän CSV-lataukset käyttävät molemmat tätä sarakelistaa.

> **Huom:** `public-columns` on erillinen sarakelista, jota käyttää julkisen sivuston CSV-lataus (`/api/public/.../csv/...`, `ethaku.svelte`). Se ei kuulu tämän featuren piiriin.

`private-columns` **ei sisällä ET2026-spesifisiä kenttiä**. Seuraavat ET2026:n uudet kentät puuttuvat:

#### Perustiedot
- `[:perustiedot :havainnointikayntityyppi-id]`

#### Lähtötiedot
- `[:lahtotiedot :energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin]`
- `[:lahtotiedot :lammitys :lammonjako-lampotilajousto]`

#### Tulokset – uusiutuvat omavaraisenergiat (kokonaistuotanto)
- `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkosahko]`
- `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :aurinkolampo]`
- `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :tuulisahko]`
- `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :lampopumppu]`
- `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :muulampo]`
- `[:tulokset :uusiutuvat-omavaraisenergiat-kokonaistuotanto :muusahko]`

#### Toteutunut ostoenergiankulutus
- `[:toteutunut-ostoenergiankulutus :tietojen-alkuperavuosi]`
- `[:toteutunut-ostoenergiankulutus :lisatietoja-fi]`
- `[:toteutunut-ostoenergiankulutus :lisatietoja-sv]`
- `[:toteutunut-ostoenergiankulutus :uusiutuvat-polttoaineet-vuosikulutus-yhteensa]`
- `[:toteutunut-ostoenergiankulutus :fossiiliset-polttoaineet-vuosikulutus-yhteensa]`
- `[:toteutunut-ostoenergiankulutus :uusiutuva-energia-vuosituotto-yhteensa]`

#### Huomiot – kasvihuonepäästöjen muutos (toimenpiteet)
ET2026:n `Huomio2026`-skeema lisää jokaiseen toimenpiteeseen kentän `:kasvihuonepaastojen-muutos`. Tämä kenttä puuttuu CSV-sarakkeista kaikista huomiokategorioista (`iv-ilmastointi`, `valaistus-muut`, `lammitys`, `ymparys`, `alapohja-ylapohja`).

Lisäksi `lammitys`-huomiossa: `[:huomiot :lammitys :kayttoikaa-jaljella-arvio-vuosina]`.

#### Ilmastoselvitys
Kokonaan uusi osio:
- `[:ilmastoselvitys :laatimisajankohta]`
- `[:ilmastoselvitys :laatija]`
- `[:ilmastoselvitys :yritys]`
- `[:ilmastoselvitys :yritys-osoite]`
- `[:ilmastoselvitys :yritys-postinumero]`
- `[:ilmastoselvitys :yritys-postitoimipaikka]`
- `[:ilmastoselvitys :laadintaperuste]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennus :rakennustuotteiden-valmistus]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennus :kuljetukset-tyomaavaihe]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennus :rakennustuotteiden-vaihdot]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennus :energiankaytto]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennus :purkuvaihe]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :rakennustuotteiden-valmistus]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :kuljetukset-tyomaavaihe]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :rakennustuotteiden-vaihdot]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :energiankaytto]`
- `[:ilmastoselvitys :hiilijalanjalki :rakennuspaikka :purkuvaihe]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennus :uudelleenkaytto]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennus :kierratys]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennus :ylimaarainen-uusiutuvaenergia]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennus :hiilivarastovaikutus]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennus :karbonatisoituminen]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :uudelleenkaytto]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :kierratys]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :ylimaarainen-uusiutuvaenergia]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :hiilivarastovaikutus]`
- `[:ilmastoselvitys :hiilikadenjalki :rakennuspaikka :karbonatisoituminen]`

**Tarvittava muutos:** `private-columns`-lista tulee laajentaa sisältämään edellä listatut ET2026-kentät. Sarakkeet tulee lisätä sopiviin kohtiin olemassa olevaa sarakejärjestystä (luonteva paikka on vastaavien 2018-kenttien jälkeen tai lopussa).

> **Huomio:** ET2013- ja ET2018-todistuksissa nämä sarakkeet ovat tyhjiä (`nil`), koska näitä kenttiä ei ole näissä versioissa. Nykyinen `energiatodistus->csv-line`-funktio käsittelee tämän oikein `get-in`-kutsulla, joka palauttaa `nil` puuttuville poluille.

### Frontend: Hakukomponentti ja skeema

Frontend-hakukomponentti (`energiatodistus-haku.svelte`) valitsee hakulomakkeen kentät käyttäjäroolin perusteella:
- **Pääkäyttäjä/laskuttaja**: `paakayttajaSchema` (lähes kaikki kentät)
- **Laatija**: `laatijaSchema` (rajoitettu osajoukko)

`laatijaSchema` (tiedostossa `schema.js`) muodostetaan karsimalla täydestä skeemasta pois kenttiä, kuten `korvattu-energiatodistus-id`, `kiinteistotunnus`, `keskeiset-suositukset`, `lahtotiedot` (paitsi `lammitetty-nettoala`), `tulokset` (paitsi `e-luku` ja `e-luokka`), `toteutunut-ostoenergiankulutus`, `huomiot`, ym.

**Tarvittava muutos:** Pääkäyttäjäskeemaan tulee lisätä kaikki ET2026-hakukentät. Laatijalle lisätään vain seuraavat ilmastoselvityksen peruskentät:
- `ilmastoselvitys.laatimisajankohta` (`dateComparisons`)
- `ilmastoselvitys.laatija` (`stringComparisons`)
- `ilmastoselvitys.yritys` (`stringComparisons`)
- `ilmastoselvitys.yritys-osoite` (`stringComparisons`)
- `ilmastoselvitys.yritys-postinumero` (`stringComparisons`)
- `ilmastoselvitys.yritys-postitoimipaikka` (`stringComparisons`)

Muut ET2026-kentät (esim. `havainnointikayntityyppi-id`, hiilijalanjälki/kädenjälki, `energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`, `kasvihuonepaastojen-muutos` ym.) näytetään vain pääkäyttäjälle. `laatijaSchema`-karsinnassa tulee varmistaa, ettei yllä listattuja ilmastoselvityskenttiä poisteta.

### Frontend: CSV-latauslinkki

CSV-latauslinkki muodostetaan tiedostossa `energiatodistukset.svelte`. Kun ET2026 on käytössä (`isEtp2026Enabled`), CSV-linkki näytetään kaikille käyttäjille (myös laatijalle). Muutoin se näytetään vain pääkäyttäjälle/laskuttajalle.

Tämä on jo toteutettu oikein. Linkki osoittaa osoitteeseen `/api/private/energiatodistukset/csv/energiatodistukset.csv` ja sisältää hakuparametrit `queryStringForExport`-muuttujasta. **Tässä ei tarvita muutoksia.**

---

## Tarvittavat muutokset (yhteenveto)

### 1. Backend: Hakukentät (private-search-schema)

`private-search-schema` tulee laajentaa `Energiatodistus2026`-skeemalla, jotta ET2026-spesifisillä kentillä voi hakea. Sama koskee `public-search-schema`-määrittelyä, mikäli julkisessa haussa halutaan tukea ET2026-kenttiä.

### 2. Backend: CSV-sarakkeiden laajentaminen

`private-columns` tulee laajentaa ET2026:n uusilla kentillä. Muut sarakelistat (`public-columns`, `anonymized-columns`, `tilastokeskus-columns`, `bank-columns`) eivät kuulu tämän featuren piiriin.

### 3. Frontend: Hakuskeema (schema.js)

`schema.js`-tiedostoon tulee lisätä kaikki ET2026-spesifiset hakukentät. Laatijalle (`laatijaSchema`) näytetään vain ilmastoselvityksen peruskentät (`laatimisajankohta`, `laatija`, `yritys`, `yritys-osoite`, `yritys-postinumero`, `yritys-postitoimipaikka`). Kaikki muut ET2026-kentät näkyvät vain pääkäyttäjälle.

### 4. Frontend: Hakukentätyypit ja operaattorit

Uusille kentille tulee määritellä operaattorityypit `schema.js`-tiedostoon:
- `havainnointikayntityyppi-id`: Tarvitsee oman luokittelu-OPERATOR_TYPE:n tai käytetään olemassa olevaa numeerista vertailua.
- Boolean-kentät (`energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`, `lammonjako-lampotilajousto`): `singleBoolean`.
- Numeeriset kentät (ilmastoselvityksen luvut): `numberComparisons`.
- Päivämääräkentät (`ilmastoselvitys.laatimisajankohta`): `dateComparisons`.
- Merkkijonokentät (`ilmastoselvitys.laatija`, `ilmastoselvitys.yritys` ym.): `stringComparisons`.

---

## Rajaustapaukset ja reunaehdot

1. **Versioiden sekoittuminen CSV:ssä:** Kun haku palauttaa sekä ET2013-, ET2018- ja ET2026-todistuksia, CSV:ssä on kaikki sarakkeet. Vanhempien versioiden kohdalla uudet sarakkeet ovat tyhjiä. Tämä on jo olemassa oleva käytäntö eikä vaadi erityiskäsittelyä.

2. **Oikeudet:** Laatija näkee aina vain omat todistuksensa (SQL-tason rajaus `energiatodistus.laatija_id = ?`). CSV-lataus käyttää samaa hakua, joten tämä rajaus pysyy automaattisesti voimassa.

3. **Hakutulosten rajoitus:** CSV-lataus käyttää `reducible-search`-funktiota, joka hakee tulokset streamina ilman `enforce-query-limit`-rajoitusta (500 kpl). Tämä on suunniteltu käytös CSV-latauksia varten.

4. **`dissoc-not-in-2026`:** ET2026-skeema poistaa kentän `polttoaineet-vuosikulutus-yhteensa` (`toteutunut-ostoenergiankulutus`). Tämä tarkoittaa, ettei kyseistä kenttää tule lisätä ET2026-spesifisiin CSV-sarakkeisiin.

5. **`Huomio2026` vs `Huomio` (2018):** ET2026:n huomiotoimenpiteissä on yksi lisäkenttä (`kasvihuonepaastojen-muutos`). CSV-sarakkeet tulee lisätä jokaiselle huomiokategorialle ja jokaiselle toimenpideindeksille (0–2).

6. **ETP2026-feature flag:** Frontend tarkistaa `isEtp2026Enabled(config)` ennen kuin näyttää ET2026-spesifisiä elementtejä. Uudet hakukentät ja CSV-sarakkeet tulee noudattaa samaa logiikkaa, tai ne voivat olla aina mukana (hakukenttien lisääminen backendiin ei riipu feature flagista, koska ne eivät palauta tuloksia, jos ET2026-todistuksia ei ole).

---

## Lokalisointi: olemassa olevat ja puuttuvat käännökset

### Jo olemassa olevat käännökset (fi.json / sv.json)

Hakukenttien labelit haetaan `propertyLabel`-funktion kautta i18n-avaimesta `energiatodistus.{polku}`. Seuraavat avaimet löytyvät jo:

- `energiatodistus.perustiedot.havainnointikayntityyppi-id` → "Havainnointikäynti toteutettu" / "Havainnointikäynti toteutettu (sv)"
- `energiatodistus.toteutunut-ostoenergiankulutus.tietojen-alkuperavuosi` → "Tiedot ovat vuodelta" / "Tiedot ovat vuodelta (sv)"
- `energiatodistus.huomiot.*.toimenpide.*.kasvihuonepaastojen-muutos` → "Energiankäytön kasvihuone­kaasupäästöjen muutos" / "(sv)"
- `energiatodistus.huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina` → "Arvio lämmitysjärjestelmän jäljellä olevasta teknisestä käyttöiästä" / "(sv)"
- `energiatodistus.ilmastoselvitys.*` — kaikki ilmastoselvityksen kentät (laatimisajankohta, laatija, yritys, yritys-osoite, yritys-postinumero, yritys-postitoimipaikka, laadintaperuste, hiilijalanjälki, hiilikädenjälki -alikentät)
- `energiatodistus.toteutunut-ostoenergiankulutus.lisatietoja` → tarkistettava, onko olemassa

### Puuttuvat käännökset — lisättävä fi.json ja sv.json -tiedostoihin

#### fi.json (`energiatodistus`-objektin sisälle)

| i18n-avain (suhteessa `energiatodistus.`) | fi | sv (dummy) |
|---|---|---|
| `lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin` | "Energiankulutuksen valmius reagoida ulkoisiin signaaleihin" | "Energiankulutuksen valmius reagoida ulkoisiin signaaleihin (sv)" |
| `lahtotiedot.lammitys.lammonjako-lampotilajousto` | "Lämmönjaon lämpötilajousto" | "Lämmönjaon lämpötilajousto (sv)" |
| `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.label-context` | "Uusiutuvat omavaraisenergiat kokonaistuotanto" | "Uusiutuvat omavaraisenergiat kokonaistuotanto (sv)" |
| `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko` | "Aurinkosähkö" | "Aurinkosähkö (sv)" |
| `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo` | "Aurinkolämpö" | "Aurinkolämpö (sv)" |
| `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko` | "Tuulisähkö" | "Tuulisähkö (sv)" |
| `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu` | "Lämpöpumppu" | "Lämpöpumppu (sv)" |
| `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo` | "Muu lämpö" | "Muu lämpö (sv)" |
| `tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko` | "Muu sähkö" | "Muu sähkö (sv)" |
| `toteutunut-ostoenergiankulutus.uusiutuvat-polttoaineet-vuosikulutus-yhteensa` | "Uusiutuvat polttoaineet vuosikulutus yhteensä" | "Uusiutuvat polttoaineet vuosikulutus yhteensä (sv)" |
| `toteutunut-ostoenergiankulutus.fossiiliset-polttoaineet-vuosikulutus-yhteensa` | "Fossiiliset polttoaineet vuosikulutus yhteensä" | "Fossiiliset polttoaineet vuosikulutus yhteensä (sv)" |
| `toteutunut-ostoenergiankulutus.uusiutuva-energia-vuosituotto-yhteensa` | "Uusiutuva energia vuosituotto yhteensä" | "Uusiutuva energia vuosituotto yhteensä (sv)" |

> **Huom:** sv-käännökset ovat dummyja (sama kuin fi + " (sv)"). Asiantuntija korvaa ne oikeilla ruotsinkielisillä käännöksillä myöhemmin.
