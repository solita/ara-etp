# TDD-suunnitelma: ET2026-hakukentät ja CSV (Frontend)

Perustuu spesifikaatioon: `energiatodistus-search-et26.spec.md`

---

## Osa 1: Hakuskeema — `schema.js` (uusi testitiedosto)

> **Luodaan uusi testitiedosto:**
> `etp-front/src/pages/energiatodistus/energiatodistus-haku/schema_test.js`
>
> Hakuskeemalle (`schema.js`) ei ole olemassa erillistä testitiedostoa. Luodaan uusi.

### 1.1 `paakayttajaSchema` sisältää ET2026-hakukentät

- Testiskenaario: `paakayttajaSchema` sisältää avaimen `energiatodistus.perustiedot.havainnointikayntityyppi-id`.
- Testiskenaario: `paakayttajaSchema` sisältää avaimen `energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`.
- Testiskenaario: `paakayttajaSchema` sisältää avaimen `energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto`.
- Testiskenaario: `paakayttajaSchema` sisältää avaimet `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko`, `.aurinkolampo`, `.tuulisahko`, `.lampopumppu`, `.muulampo`, `.muusahko`.
- Testiskenaario: `paakayttajaSchema` sisältää avaimet `energiatodistus.toteutunut-ostoenergiankulutus.tietojen-alkuperavuosi`, `.uusiutuvat-polttoaineet-vuosikulutus-yhteensa`, `.fossiiliset-polttoaineet-vuosikulutus-yhteensa`, `.uusiutuva-energia-vuosituotto-yhteensa`.
- Testiskenaario: `paakayttajaSchema` sisältää `energiatodistus.ilmastoselvitys.*` -kentät (kaikki 7 peruskenttiä + `laadintaperuste` + `hiilijalanjalki.*` + `hiilikadenjalki.*`).
- Testiskenaario: `paakayttajaSchema` sisältää `energiatodistus.huomiot.*.toimenpide.*.kasvihuonepaastojen-muutos` -kentät (tai vastaava tasoitettu polku jokaiselle huomiokategorialle).
- Testiskenaario: `paakayttajaSchema` sisältää `energiatodistus.huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina`.

### 1.2 `laatijaSchema` sisältää ilmastoselvityksen peruskentät

- Testiskenaario: `laatijaSchema` sisältää avaimen `energiatodistus.ilmastoselvitys.laatimisajankohta`.
- Testiskenaario: `laatijaSchema` sisältää avaimen `energiatodistus.ilmastoselvitys.laatija`.
- Testiskenaario: `laatijaSchema` sisältää avaimen `energiatodistus.ilmastoselvitys.yritys`.
- Testiskenaario: `laatijaSchema` sisältää avaimen `energiatodistus.ilmastoselvitys.yritys-osoite`.
- Testiskenaario: `laatijaSchema` sisältää avaimen `energiatodistus.ilmastoselvitys.yritys-postinumero`.
- Testiskenaario: `laatijaSchema` sisältää avaimen `energiatodistus.ilmastoselvitys.yritys-postitoimipaikka`.

### 1.3 `laatijaSchema` EI sisällä muita ET2026-kenttiä

- Testiskenaario: `laatijaSchema` EI sisällä avainta `energiatodistus.perustiedot.havainnointikayntityyppi-id`.
- Testiskenaario: `laatijaSchema` EI sisällä avainta `energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`.
- Testiskenaario: `laatijaSchema` EI sisällä avainta `energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto`.
- Testiskenaario: `laatijaSchema` EI sisällä avaimia `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.*`.
- Testiskenaario: `laatijaSchema` EI sisällä avaimia `energiatodistus.ilmastoselvitys.hiilijalanjalki.*`.
- Testiskenaario: `laatijaSchema` EI sisällä avaimia `energiatodistus.ilmastoselvitys.hiilikadenjalki.*`.
- Testiskenaario: `laatijaSchema` EI sisällä avainta `energiatodistus.ilmastoselvitys.laadintaperuste`.
- Testiskenaario: `laatijaSchema` EI sisällä avaimia `energiatodistus.huomiot.*.toimenpide.*.kasvihuonepaastojen-muutos`.

### 1.4 Operaattorityypit ovat oikein uusille kentille

- Testiskenaario: `flatSchema['energiatodistus.perustiedot.havainnointikayntityyppi-id']` sisältää oikean operaattorityypin (numeerinen tai luokittelu).
- Testiskenaario: `flatSchema['energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin']`-operaattorityyppi on `BOOLEAN`.
- Testiskenaario: `flatSchema['energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto']`-operaattorityyppi on `BOOLEAN`.
- Testiskenaario: `flatSchema['energiatodistus.ilmastoselvitys.laatimisajankohta']`-operaattorityyppi on `DATE`.
- Testiskenaario: `flatSchema['energiatodistus.ilmastoselvitys.laatija']`-operaattorityyppi on `STRING`.
- Testiskenaario: Numeeriset ilmastoselvityskentät (esim. `hiilijalanjalki.rakennus.rakennustuotteiden-valmistus`) ovat tyyppiä `NUMBER`.

---

## Osa 2: Hakuapufunktiot (energiatodistus-haku-utils_test.js)

> **Muokataan olemassa olevaa testitiedostoa:**
> `etp-front/src/pages/energiatodistus/energiatodistus-haku/energiatodistus-haku-utils_test.js`

### 2.1 `blockToQueryParameter` — ET2026-kenttä

- Testiskenaario: `blockToQueryParameter` muuntaa `['sisaltaa', 'energiatodistus.ilmastoselvitys.laatija', 'Matti']` oikeaksi kyselyparametriksi `[['ilike', 'energiatodistus.ilmastoselvitys.laatija', '%Matti%']]`.
- Testiskenaario: `blockToQueryParameter` muuntaa `['=', 'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin', true]` oikeaksi kyselyparametriksi.

### 2.2 `convertWhereToQuery` — ET2026-kenttiä sisältävä where

- Testiskenaario: Monimutkainen where-lauseke joka sisältää sekä vanhoja (esim. `perustiedot.nimi-fi`) että ET2026-kenttiä (`ilmastoselvitys.yritys`) muunnetaan oikein.

---

## Osa 3: Lokalisointi — `fi.json` ja `sv.json` (ei testitiedostoa)

Frontend-lokalisoinnille ei ole erillistä yksikkötestiä. Käännösten olemassaolo validoidaan epäsuorasti schema-testien kautta (kentät vaativat olemassa olevia i18n-avaimia näkyäkseen hakulomakkeella oikein), mutta varsinaista lokaalisointitestiä ei lisätä.

Puuttuvat käännösavaimet on listattu spesifikaation Lokalisointi-osiossa.
