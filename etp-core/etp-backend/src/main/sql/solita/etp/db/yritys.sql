-- name: insert-yritys<!
INSERT INTO yritys (ytunnus, nimi, verkkolaskuoperaattori, verkkolaskuosoite, laskutuskieli, jakeluosoite, vastaanottajan_tarkenne, postinumero, postitoimipaikka, maa, type_id)
VALUES (:ytunnus, :nimi, :verkkolaskuoperaattori, :verkkolaskuosoite, :laskutuskieli, :jakeluosoite, :vastaanottajan-tarkenne, :postinumero, :postitoimipaikka, :maa, :type-id)
RETURNING id

-- name: update-yritys!
update yritys set nimi = :nimi, ytunnus = :ytunnus, verkkolaskuoperaattori = :verkkolaskuoperaattori, verkkolaskuosoite = :verkkolaskuosoite, laskutuskieli = :laskutuskieli,
       jakeluosoite = :jakeluosoite, vastaanottajan_tarkenne = :vastaanottajan-tarkenne,
       postinumero = :postinumero, postitoimipaikka = :postitoimipaikka, maa = :maa,
       type_id = :type-id
where id = :id

-- name: select-yritys
select id, ytunnus, nimi, verkkolaskuoperaattori, verkkolaskuosoite, laskutuskieli, deleted,
       jakeluosoite, vastaanottajan_tarkenne as "vastaanottajan-tarkenne", postinumero, postitoimipaikka, maa, type_id
from yritys
where id = :id

-- name: select-all-yritykset
select id, ytunnus, nimi, verkkolaskuoperaattori, verkkolaskuosoite, laskutuskieli, deleted,
       jakeluosoite, vastaanottajan_tarkenne as "vastaanottajan-tarkenne", postinumero, postitoimipaikka, maa, type_id
from yritys

--name: select-all-laskutuskielet
SELECT id, label_fi as "label-fi", label_sv as "label-sv", valid FROM laskutuskieli;

-- name: select-all-verkkolaskuoperaattorit
SELECT id, valittajatunnus, nimi FROM verkkolaskuoperaattori order by nimi collate "fi-FI-x-icu", valittajatunnus;

-- name: select-laatijat
with audit as (
  select row_number() over (
    partition by laatija_id
    order by modifytime desc, event_id desc) as event_order,
         modifytime,
         modifiedby_id,
         laatija_id
  from audit.laatija_yritys audit
  where yritys_id = :id
)
select
  laatija.id,
  laatija.etunimi,
  laatija.sukunimi,
  audit.modifytime,
  fullname(modifier) modifiedby_name,
  laatija_yritys.tila_id
from laatija_yritys
  inner join kayttaja laatija on laatija.id = laatija_yritys.laatija_id
  left join audit on audit.laatija_id = laatija_yritys.laatija_id and
                     audit.event_order = 1
  left join kayttaja modifier on modifier.id = audit.modifiedby_id
where laatija_yritys.yritys_id = :id

-- name: insert-laatija-yritys!
insert into laatija_yritys (laatija_id, yritys_id, tila_id)
values (:laatija-id, :yritys-id, 1)
on conflict (laatija_id, yritys_id) do update set tila_id = 1

-- name: update-yritys-deleted!
update yritys set deleted = :deleted where id = :id;

--name: select-laatija-by-id
select
  l.id,
  l.partner
from laatija l where l.id = :id
