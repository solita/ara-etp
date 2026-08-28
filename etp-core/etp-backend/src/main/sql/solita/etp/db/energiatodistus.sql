
-- name: delete-energiatodistus-luonnos!
update energiatodistus set
  tila_id = et_tilat.poistettu,
  korvattu_energiatodistus_id = null
from et_tilat
where tila_id = et_tilat.luonnos and id = :id

-- name: discard-energiatodistus!
update energiatodistus set tila_id = et_tilat.hylatty
from et_tilat
where tila_id = et_tilat.allekirjoitettu and id = :id

-- name: undo-discard-energiatodistus!
update energiatodistus set tila_id = et_tilat.allekirjoitettu
from et_tilat
where tila_id = et_tilat.hylatty and id = :id

-- name: select-energiatodistus
select energiatodistus.*,
       fullname(kayttaja.*) "laatija-fullname",
       korvaava_energiatodistus.id as korvaava_energiatodistus_id,
       ppp.id as perusparannuspassi_id,
       ppp.valid as perusparannuspassi_valid
from energiatodistus
  inner join kayttaja on kayttaja.id = energiatodistus.laatija_id
  left join energiatodistus korvaava_energiatodistus on korvaava_energiatodistus.korvattu_energiatodistus_id = energiatodistus.id
  left join perusparannuspassi ppp on ppp.energiatodistus_id = energiatodistus.id
where energiatodistus.id = :id
  and energiatodistus.tila_id <> (select poistettu FROM et_tilat);

-- name: update-energiatodistus-allekirjoituksessa!
update energiatodistus set tila_id = et_tilat.allekirjoituksessa
from et_tilat
where tila_id = et_tilat.luonnos and laatija_id = :laatija-id and id = :id

-- name: update-energiatodistus-luonnos!
update energiatodistus set tila_id = et_tilat.luonnos
from et_tilat
where tila_id = et_tilat.allekirjoituksessa and laatija_id = :laatija-id and id = :id

-- name: update-energiatodistus-allekirjoitettu!
update energiatodistus set
  tila_id = et_tilat.allekirjoitettu,
  allekirjoitusaika = :allekirjoitusaika,
  voimassaolo_paattymisaika = :voimassaolo-paattymisaika
from et_tilat
where tila_id = et_tilat.allekirjoituksessa and laatija_id = :laatija-id and id = :id

-- name: reset-toimenpide-ehdotukset-and-suositukset!
update energiatodistus
set
  pt$keskeiset_suositukset_fi = null,
  pt$keskeiset_suositukset_sv = null,
  h$alapohja_ylapohja$teksti_fi = null,
  h$alapohja_ylapohja$teksti_sv = null,
  h$alapohja_ylapohja$toimenpide = null,
  h$iv_ilmastointi$teksti_fi = null,
  h$iv_ilmastointi$teksti_sv = null,
  h$iv_ilmastointi$toimenpide = null,
  h$lammitys$teksti_fi = null,
  h$lammitys$teksti_sv = null,
  h$lammitys$toimenpide = null,
  h$valaistus_muut$teksti_fi = null,
  h$valaistus_muut$teksti_sv = null,
  h$valaistus_muut$toimenpide = null,
  h$ymparys$teksti_fi = null,
  h$ymparys$teksti_sv = null,
  h$ymparys$toimenpide = null,
  h$suositukset_fi = null,
  h$suositukset_sv = null,
  h$lammitys$kayttoikaa_jaljella_arvio_vuosina = null
where id = :id;

-- name: update-energiatodistus-korvattu!
update energiatodistus set
  tila_id = et_tilat.korvattu
from et_tilat
where tila_id in (et_tilat.allekirjoitettu, et_tilat.hylatty) and id = :id

-- name: revert-energiatodistus-korvattu!
update energiatodistus set
  tila_id = coalesce((
    select history.tila_id from audit.energiatodistus_tila history
    where history.id = energiatodistus.id
    order by history.modifytime desc, history.event_id desc limit 1 offset 1),
  et_tilat.allekirjoitettu)
from et_tilat
where tila_id = et_tilat.korvattu and id = :id

-- name: select-numeric-validations
select column_name, warning$min, warning$max, error$min, error$max
from validation_numeric_column where versio = :versio;

-- name: select-required-columns
select column_name
from validation_required_column
where versio = :versio and valid and not (bypass_allowed and :bypass-validation)
order by ordinal asc;

-- name: select-sisaiset-kuormat
select
  kayttotarkoitusluokka_id,
  henkilot$kayttoaste,
  henkilot$lampokuorma,
  kuluttajalaitteet$kayttoaste,
  kuluttajalaitteet$lampokuorma,
  valaistus$kayttoaste,
  valaistus$lampokuorma
from validation_sisainen_kuorma where versio = :versio;

-- name: select-korvattavat
select energiatodistus.*,
  fullname(kayttaja.*) "laatija-fullname",
  korvaava_energiatodistus.id as korvaava_energiatodistus_id,
  ppp.id as perusparannuspassi_id
from energiatodistus
  inner join kayttaja on kayttaja.id = energiatodistus.laatija_id
  left join energiatodistus korvaava_energiatodistus on korvaava_energiatodistus.korvattu_energiatodistus_id = energiatodistus.id
  left join perusparannuspassi ppp on ppp.energiatodistus_id = energiatodistus.id
where energiatodistus.tila_id in (select allekirjoitettu from et_tilat) and (
  energiatodistus.pt$rakennustunnus = :rakennustunnus or
  (energiatodistus.pt$postinumero = :postinumero and
    (energiatodistus.pt$katuosoite_fi = :katuosoite-fi or
     energiatodistus.pt$katuosoite_sv = :katuosoite-sv)))
order by
  case when energiatodistus.pt$rakennustunnus = :rakennustunnus then 1 end nulls last,
  energiatodistus.allekirjoitusaika desc
limit 10;

-- name: select-protected-postinumero-versio-kayttotarkoitus
select
  versio,
  pt$kayttotarkoitus as kayttotarkoitus,
  lpad(pt$postinumero::text, 5, '0') as postinumero
from energiatodistus
where tila_id in (2, 4)
group by versio,pt$kayttotarkoitus, pt$postinumero
having
  count(*) < :min-count and
  (versio, pt$kayttotarkoitus) in (
    (2013, 'YAT'),
    (2013, 'KAT'),
    (2013, 'MEP'),
    (2013, 'MAEP'),
    (2018, 'YAT'),
    (2018, 'KAT'),
    (2018, 'KREP')
  )
order by pt$postinumero;

-- name: reset-ilmastoselvitys!
update energiatodistus
set
    is$laatija = null,
    is$yritys = null,
    is$yritys_osoite = null,
    is$yritys_postinumero = null,
    is$yritys_postitoimipaikka = null,
    is$laadintaperuste = null,
    is$hiilijalanjalki$rakennus$rakennustuotteiden_valmistus = null,
    is$hiilijalanjalki$rakennus$kuljetukset_tyomaavaihe = null,
    is$hiilijalanjalki$rakennus$rakennustuotteiden_vaihdot  = null,
    is$hiilijalanjalki$rakennus$energiankaytto = null,
    is$hiilijalanjalki$rakennus$purkuvaihe = null,
    is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_valmistus = null,
    is$hiilijalanjalki$rakennuspaikka$kuljetukset_tyomaavaihe = null,
    is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_vaihdot = null,
    is$hiilijalanjalki$rakennuspaikka$energiankaytto = null,
    is$hiilijalanjalki$rakennuspaikka$purkuvaihe = null,
    is$hiilikadenjalki$rakennus$uudelleenkaytto = null,
    is$hiilikadenjalki$rakennus$kierratys = null,
    is$hiilikadenjalki$rakennus$ylimaarainen_uusiutuvaenergia = null,
    is$hiilikadenjalki$rakennus$hiilivarastovaikutus = null,
    is$hiilikadenjalki$rakennus$karbonatisoituminen = null,
    is$hiilikadenjalki$rakennuspaikka$uudelleenkaytto = null,
    is$hiilikadenjalki$rakennuspaikka$kierratys = null,
    is$hiilikadenjalki$rakennuspaikka$ylimaarainen_uusiutuvaenergia = null,
    is$hiilikadenjalki$rakennuspaikka$hiilivarastovaikutus = null,
    is$hiilikadenjalki$rakennuspaikka$karbonatisoituminen = null,
    is$hiilikadenjalki$rakennus$hyodyntaminen_energiana = null,
    is$hiilikadenjalki$rakennuspaikka$hyodyntaminen_energiana = null
where id = :id;

-- name: reset-finnish-fields!
update energiatodistus
set
    pt$katuosoite_fi = null,
    pt$keskeiset_suositukset_fi = null,
    lt$ilmanvaihto$kuvaus_fi = null,
    lt$lammitys$lammitysmuoto_1$kuvaus_fi = null,
    lt$lammitys$lammitysmuoto_2$kuvaus_fi = null,
    lt$lammitys$lammonjako$kuvaus_fi = null,
    h$alapohja_ylapohja$teksti_fi = null,
    h$iv_ilmastointi$teksti_fi = null,
    h$lammitys$teksti_fi = null,
    h$valaistus_muut$teksti_fi = null,
    h$ymparys$teksti_fi = null,
    h$suositukset_fi = null,
    lisamerkintoja_fi = null,
    pt$nimi_fi = null,
    to$lisatietoja_fi = null,
    h$alapohja_ylapohja$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW(NULL, (elem).nimi_sv, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$alapohja_ylapohja$toimenpide) AS elem
        )
    ),
    h$iv_ilmastointi$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW(NULL, (elem).nimi_sv, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$iv_ilmastointi$toimenpide) AS elem
        )
    ),
    h$lammitys$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW(NULL, (elem).nimi_sv, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$lammitys$toimenpide) AS elem
        )
    ),
    h$valaistus_muut$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW(NULL, (elem).nimi_sv, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$valaistus_muut$toimenpide) AS elem
        )
    ),
    h$ymparys$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW(NULL, (elem).nimi_sv, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$ymparys$toimenpide) AS elem
        )
    )
where id = :id;

-- name: reset-swedish-fields!
update energiatodistus
set
    pt$katuosoite_sv = null,
    pt$keskeiset_suositukset_sv = null,
    lt$ilmanvaihto$kuvaus_sv = null,
    lt$lammitys$lammitysmuoto_1$kuvaus_sv = null,
    lt$lammitys$lammitysmuoto_2$kuvaus_sv = null,
    lt$lammitys$lammonjako$kuvaus_sv = null,
    h$alapohja_ylapohja$teksti_sv = null,
    h$iv_ilmastointi$teksti_sv = null,
    h$lammitys$teksti_sv = null,
    h$valaistus_muut$teksti_sv = null,
    h$ymparys$teksti_sv = null,
    h$suositukset_sv = null,
    lisamerkintoja_sv = null,
    pt$nimi_sv = null,
    to$lisatietoja_sv = null,
    h$alapohja_ylapohja$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW((elem).nimi_fi, NULL, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$alapohja_ylapohja$toimenpide) AS elem
        )
    ),
    h$iv_ilmastointi$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW((elem).nimi_fi, NULL, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$iv_ilmastointi$toimenpide) AS elem
        )
    ),
    h$lammitys$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW((elem).nimi_fi, NULL, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$lammitys$toimenpide) AS elem
        )
    ),
    h$valaistus_muut$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW((elem).nimi_fi, NULL, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$valaistus_muut$toimenpide) AS elem
        )
    ),
    h$ymparys$toimenpide = (
        SELECT ARRAY(
                   SELECT ROW((elem).nimi_fi, NULL, (elem).lampo, (elem).sahko, (elem).jaahdytys, (elem).eluvun_muutos, (elem).kasvihuonepaastojen_muutos)::toimenpide
            FROM unnest(h$ymparys$toimenpide) AS elem
        )
    )
where id = :id;
