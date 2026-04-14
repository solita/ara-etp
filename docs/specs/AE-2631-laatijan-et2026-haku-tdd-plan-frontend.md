# AE-2631 Laatijan ET2026-haku — TDD-suunnitelma (Frontend)

Testitiedosto: `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema_test.js` (olemassa oleva)

Kaikki alla olevat testit muokataan tai lisätään olemassa olevaan testitiedostoon.

---

## 1. Laatijan skeeman kenttäinventaario (olemassa olevan testin muokkaus)

**Testi:** `laatijaSchema contains exactly the expected fields`

Olemassa oleva testi (rivi 200) tarkistaa laatijan skeeman kentät täsmällisesti. Testin odotettuun kenttälistaan lisätään kolme uutta kenttää:

- `energiatodistus.perustiedot.havainnointikayntityyppi-id`
- `energiatodistus.tulokset.kasvihuonepaastot-per-nelio`
- `energiatodistus.tulokset.uusiutuvan-energian-osuus`

Tämä on TDD:n punainen vaihe: testi päivitetään ensin, ja se epäonnistuu kunnes `schema.js`-muutokset tehdään.

---

## 2. Versio-kenttä on jo laatijan skeemassa (uusi testi)

**Testi:** `laatijaSchema includes energiatodistus.versio`

Varmistaa, että `energiatodistus.versio` -kenttä löytyy `laatijaSchema`sta. Speksissä todetaan, ettei tämä vaadi muutoksia, mutta se tulee varmistaa testillä regressiosuojana.

---

## 3. E-luokka on jo laatijan skeemassa (uusi testi)

**Testi:** `laatijaSchema includes energiatodistus.tulokset.e-luokka`

Varmistaa, että `energiatodistus.tulokset.e-luokka` -kenttä löytyy `laatijaSchema`sta. Regressiosuoja — kenttä on jo olemassa.

---

## 4. Havainnointikäyntityyppi sisältyy laatijan skeemaan (uusi testi)

**Testi:** `laatijaSchema includes energiatodistus.perustiedot.havainnointikayntityyppi-id`

Varmistaa, että kenttä löytyy `laatijaSchema`n avaimista. Tämä on uusi kenttä, joka tuodaan mukaan poistamalla se `R.omit`-listasta.

---

## 5. Havainnointikäyntityypin operaattorityyppi (uusi testi)

**Testi:** `laatijaSchema havainnointikayntityyppi-id uses correct operator type`

Varmistaa, että kenttä käyttää samaa operaatiotyyppiä kuin pääkäyttäjän skeemassa. Vertaa `laatijaSchema['energiatodistus.perustiedot.havainnointikayntityyppi-id']` ja `paakayttajaSchema['energiatodistus.perustiedot.havainnointikayntityyppi-id']` — niiden tulee olla samat.

---

## 6. Kasvihuonepäästöt per neliö sisältyy laatijan skeemaan (uusi testi)

**Testi:** `laatijaSchema includes energiatodistus.tulokset.kasvihuonepaastot-per-nelio`

Varmistaa, että kenttä löytyy `laatijaSchema`n avaimista. Tämä on uusi kenttä tulokset-osiossa.

---

## 7. Kasvihuonepäästöt per neliö — operaatioiden vastaavuus (uusi testi)

**Testi:** `laatijaSchema kasvihuonepaastot-per-nelio matches paakayttajaSchema`

Varmistaa, että `laatijaSchema`n ja `paakayttajaSchema`n kenttämääritykset (operaatiot) ovat identtiset tälle kentälle.

---

## 8. Uusiutuvan energian osuus sisältyy laatijan skeemaan (uusi testi)

**Testi:** `laatijaSchema includes energiatodistus.tulokset.uusiutuvan-energian-osuus`

Varmistaa, että kenttä löytyy `laatijaSchema`n avaimista.

---

## 9. Uusiutuvan energian osuus — operaatioiden vastaavuus (uusi testi)

**Testi:** `laatijaSchema uusiutuvan-energian-osuus matches paakayttajaSchema`

Varmistaa, että `laatijaSchema`n ja `paakayttajaSchema`n kenttämääritykset (operaatiot) ovat identtiset tälle kentälle.

---

## 10. Pääkäyttäjän skeema ei muutu (olemassa olevan testin varmistus)

**Testi:** `paakayttajaSchema contains exactly the expected fields`

Olemassa oleva testi (rivi 237). Ei vaadi muutoksia — varmistaa, ettei pääkäyttäjän skeema muutu vahingossa refaktoroinnin yhteydessä.

---

## Reunatapaukset ja erityisehdot

- **Omit-listan johdonmukaisuus:** Uusien kenttien lisäyksen jälkeen `R.omit`-lista lyhenee yhdellä. Kenttäinventaariotesti (kohta 1) havaitsee, jos jokin kenttä jää vahingossa pois tai ylimääräisiä kenttiä vuotaa läpi.
- **Pick-listan täydellisyys:** `R.pick`-listan muutos tulokset-osiossa koskee vain nimettyjen kenttien lisäystä. Kenttäinventaariotesti kattaa myös tämän — jos pick-lista on väärä, kenttiä puuttuu tai on liikaa.
- **Kenttien operaatioiden yhteneväisyys:** Uudet laatijan kentät tulevat samasta pohja-skeemasta kuin pääkäyttäjän kentät. Vastaavuustestit (kohdat 5, 7, 9) varmistavat, ettei kenttien käsittelyssä tapahdu muunnoksia flattenSchema-putkessa.
