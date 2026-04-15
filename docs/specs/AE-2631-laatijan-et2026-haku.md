# AE-2631 Laatijan ET-hakutoiminnon laajennus sisältämään myös omat ET2026:t (uudet tietokentät)

## Tausta ja tarve

Pääkäyttäjän hakutoiminto (`paakayttajaSchema`) tukee jo kaikkia ET2026-kenttiä. Laatijan hakutoiminto (`laatijaSchema`) on nykyisellään huomattavasti rajoitetumpi — erityisesti `tulokset`-osiosta näkyy vain `e-luku` ja `e-luokka`. Tässä laajennetaan laatijan hakua sisältämään myös ET2026:n tuomia uusia hakukriteereitä.

## Lisättävät hakukentät

Laatijan hakuskeemaan (`laatijaSchema`) lisätään seuraavat kentät, jotka ovat jo pääkäyttäjän skeemassa:

### 1. Todistusversio sis. 2026

> **Huomio:** Versio-kenttä (`energiatodistus.versio`) on jo nyt `laatijaSchema`ssa — se kulkee läpi `flattenSchema`n, eikä sitä poisteta `R.omit`-listassa. Versio-dropdown (`OPERATOR_TYPES.VERSIO`) sisältää jo arvon 2026.

**→ Ei vaadi muutoksia**, kenttä on jo laatijan käytettävissä. Tämä tulisi varmistaa testeillä.

### 2. Energiatehokkuusluokat sis. uudet arvot (A+ ja A0)

> **Huomio:** E-luokka-kenttä (`energiatodistus.tulokset.e-luokka`) on jo `laatijaSchema`ssa (tulokset piketään `['e-luku', 'e-luokka']`). E-luokka-input-komponentti (`e-luokka-input.svelte`) renderöi jo 9 vaihtoehtoa: A+, A0, A, B, C, D, E, F, G. Tämä on toteutettu ja testattu (`e-luokka-input.test.js`).

**→ Ei vaadi muutoksia**, A+ ja A0 ovat jo käytettävissä. Tämä tulisi varmistaa testeillä.

### 3. Perustiedot / havainnointikayntityyppi

Kenttä `energiatodistus.perustiedot.havainnointikayntityyppi-id` on tällä hetkellä eksplisiittisesti poistettu laatijan skeemasta `R.omit`-listassa (schema.js rivi ~796).

**Muutos frontendissä:** Poista `'energiatodistus.perustiedot.havainnointikayntityyppi-id'` `laatijaSchema`n `R.omit`-listasta.

Kenttä käyttää operaattorityyppiä `OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI` ja `luokitteluEquals`-operaatiota, jotka ovat jo olemassa.

### 4. Energian käytöstä syntyvät kasvihuonepäästöt (per neliö)

Kenttä `energiatodistus.tulokset.kasvihuonepaastot-per-nelio` on osa `tulokset`-objektia, mutta laatijan skeema rajaa tulokset vain kenttiin `['e-luku', 'e-luokka']` (`R.pick`-kutsulla rivillä ~811).

**Muutos frontendissä:** Lisää `'kasvihuonepaastot-per-nelio'` tulokset-osion `R.pick`-listaan:
`R.pick(['e-luku', 'e-luokka', 'kasvihuonepaastot-per-nelio'])`

Kenttä on laskennallinen (computed field) backendissä (`energiatodistus_search_fields.clj`), joka laskee CO2-kertoimilla painotetun kasvihuonepäästöarvon jaettuna nettoalalla. Backend tukee tätä kenttää jo valmiiksi kaikille rooleille (`private-search-schema`).

### 5. Paikan päällä tuotetun uusiutuvan energian osuus energiankäytöstä

Kenttä `energiatodistus.tulokset.uusiutuvan-energian-osuus` on myös osa `tulokset`-objektia ja rajautuu pois samasta syystä kuin yllä.

**Muutos frontendissä:** Lisää `'uusiutuvan-energian-osuus'` tulokset-osion `R.pick`-listaan:
`R.pick(['e-luku', 'e-luokka', 'kasvihuonepaastot-per-nelio', 'uusiutuvan-energian-osuus'])`

> **Huomio:** Backendin `computed-fields`issä `uusiutuvan-energian-osuus` on tällä hetkellä kovakoodattu arvolla `"0"` (search_fields.clj rivi ~143). Tämä tarkoittaa, että haku toimii teknisesti mutta palauttaa aina 0. Tämä on olemassa oleva rajoite, joka on sama myös pääkäyttäjän haussa — ei tämän tehtävän scope, mutta hyvä tiedostaa.

## Muutosten laajuus

### Frontend (`etp-front`)

**Muutettava tiedosto:** `src/pages/energiatodistus/energiatodistus-haku/schema.js`

Muutokset kohdistuvat `laatijaSchema`-funktioon (rivit ~791–815):

1. **`R.omit`-listan muutos:** Poista `'energiatodistus.perustiedot.havainnointikayntityyppi-id'`
2. **`R.pick` tulokset-muutos:** Vaihda `R.pick(['e-luku', 'e-luokka'])` → `R.pick(['e-luku', 'e-luokka', 'kasvihuonepaastot-per-nelio', 'uusiutuvan-energian-osuus'])`

**Muutettava testitiedosto:** `src/pages/energiatodistus/energiatodistus-haku/schema_test.js`

- Päivitä `laatijaSchema contains exactly the expected fields` -testin odotettu kenttälista lisäämällä:
  - `'energiatodistus.perustiedot.havainnointikayntityyppi-id'`
  - `'energiatodistus.tulokset.kasvihuonepaastot-per-nelio'`
  - `'energiatodistus.tulokset.uusiutuvan-energian-osuus'`

### Backend (`etp-core/etp-backend`)

**Ei vaadi muutoksia.** Backend käyttää yhtä yhtenäistä `private-search-schema`a kaikille kirjautuneille käyttäjille (laatija, pääkäyttäjä, laskuttaja). Hakukenttien rajaus per rooli tehdään frontendissä skeeman avulla. Backend validoi vain, että kenttä löytyy search-schemasta, ja kaikki tarvittavat kentät ovat jo siellä.

### Lokalisointi / i18n

> **Tarkistettava:** Ovatko hakukenttien lokalisoidut nimet (label) jo olemassa uusille laatijan hakukentille? Pääkäyttäjän haussa ne ovat käytössä, joten todennäköisesti kyllä — mutta tämä tulee varmistaa.

## Toteutuksen tila

> Katselmoitu 2026-04-15. Kaikki speksin mukaiset muutokset toteutettu ja testattu.

- [x] **R.omit**: `havainnointikayntityyppi-id` poistettu omit-listasta
- [x] **R.omit**: `julkinen-rakennus` poistettu omit-listasta (oli no-op: omit kohdistui väärään polkuun `energiatodistus.julkinen-rakennus`, kenttä on `energiatodistus.perustiedot.julkinen-rakennus`)
- [x] **R.pick tulokset**: `kasvihuonepaastot-per-nelio` ja `uusiutuvan-energian-osuus` lisätty
- [x] **Testit**: Kenttäinventaario päivitetty, uudet ET2026-kenttätestit lisätty (`it.each`), skeemavastaavuustestit laatija↔pääkäyttäjä
- [x] **Ei-muutokset vahvistettu**: versio, e-luokka, backend, hakutulosten näyttö

### Poikkeamat speksistä

- `energiatodistus.julkinen-rakennus` poistettiin `R.omit`-listasta, vaikka speksi ei tätä mainitse. Kyseessä oli kuitenkin kuollut rivi (väärä polku), joten poisto on siivousluonteinen eikä muuta toiminnallisuutta.

## Ei-muutokset (vahvistettu koodikatselmuksella)

- **Versio-kenttä (2026):** Jo laatijan skeemassa, ei muutoksia
- **E-luokka (A+, A0):** Jo tuettu e-luokka-input-komponentissa, ei muutoksia
- **Backend-hakupalvelu:** Ei muutoksia, kaikki kentät jo private-search-schemassa
- **Hakutulosten näyttö:** Muutokset koskevat vain hakukriteerejä, eivät tuloslistaa
