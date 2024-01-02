
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
       korvaava_energiatodistus.id as korvaava_energiatodistus_id
from energiatodistus
  inner join kayttaja on kayttaja.id = energiatodistus.laatija_id
  left join energiatodistus korvaava_energiatodistus on korvaava_energiatodistus.korvattu_energiatodistus_id = energiatodistus.id
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
  allekirjoitusaika = now(),
  voimassaolo_paattymisaika =
    timezone('Europe/Helsinki',
      timezone('Europe/Helsinki', now())::date::timestamp without time zone
        + interval '1 day') + interval '10 year'
from et_tilat
where tila_id = et_tilat.allekirjoituksessa and laatija_id = :laatija-id and id = :id

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
  korvaava_energiatodistus.id as korvaava_energiatodistus_id
from energiatodistus
  inner join kayttaja on kayttaja.id = energiatodistus.laatija_id
  left join energiatodistus korvaava_energiatodistus on korvaava_energiatodistus.korvattu_energiatodistus_id = energiatodistus.id
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
