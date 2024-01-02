--name: select-laatijat
SELECT k.id,
       k.etunimi,
       k.sukunimi,
       k.puhelin,
       k.email,
       k.henkilotunnus,
       k.login, k.passivoitu,
       l.patevyystaso,
       l.toteamispaivamaara,
       patevyys_paattymisaika(l) voimassaolo_paattymisaika,
       patevyys_voimassa(l) as voimassa,
       l.wwwosoite,
       l.toimintaalue,
       l.muut_toimintaalueet muuttoimintaalueet,
       l.jakeluosoite,
       l.postinumero,
       l.postitoimipaikka,
       l.maa,
       l.laatimiskielto,
       l.julkinen_email julkinenemail,
       l.julkinen_puhelin julkinenpuhelin,
       l.julkinen_wwwosoite julkinenwwwosoite,
       l.julkinen_osoite julkinenosoite,
       l.julkinen_postinumero julkinenpostinumero,
       l.partner,
       array(select yritys_id from laatija_yritys where laatija_id = l.id and tila_id = 1) as yritys,
       coalesce(current_timestamp < login + interval '6 month', false) as aktiivinen
FROM laatija l
INNER JOIN kayttaja k
ON l.id = k.id
ORDER BY k.sukunimi, k.etunimi

--name: select-laatija-by-id
select
  l.id, l.patevyystaso,
  l.toteamispaivamaara, l.toteaja, l.laatimiskielto,
  patevyys_paattymisaika(l) voimassaolo_paattymisaika,
  patevyys_voimassa(l) as voimassa,
  l.toimintaalue, l.muut_toimintaalueet as muuttoimintaalueet,
  l.julkinen_puhelin as julkinenpuhelin,
  l.julkinen_email as julkinenemail,
  l.julkinen_osoite as julkinenosoite,
  l.julkinen_wwwosoite as julkinenwwwosoite,
  l.julkinen_postinumero as julkinenpostinumero,
  l.laskutuskieli,
  l.vastaanottajan_tarkenne, l.jakeluosoite,
  l.postinumero, l.postitoimipaikka, l.wwwosoite, l.maa,
  l.partner
from laatija l where l.id = :id

--name: select-laatija-by-henkilotunnus
SELECT l.id, k.henkilotunnus, l.patevyystaso,
       l.toteamispaivamaara, l.toteaja, l.laatimiskielto,
       l.toimintaalue, l.muut_toimintaalueet as muuttoimintaalueet,
       l.julkinen_puhelin as julkinenpuhelin, l.julkinen_email as julkinenemail, l.julkinen_osoite as julkinenosoite, l.julkinen_wwwosoite as julkinenwwwosoite,
       l.julkinen_postinumero as julkinenpostinumero,
       l.partner,
       l.laskutuskieli, l.vastaanottajan_tarkenne,
       l.jakeluosoite, l.postinumero, l.postitoimipaikka, l.wwwosoite, l.maa
FROM laatija l INNER JOIN kayttaja k ON l.id = k.id WHERE k.henkilotunnus = :henkilotunnus

-- name: select-laatija-yritykset
select distinct on (laatija_yritys.yritys_id)
    laatija_yritys.yritys_id id,
    laatija_yritys.tila_id,
    audit.modifytime, fullname(modifier) modifiedby_name
  from laatija_yritys
  left join audit.laatija_yritys audit
    on audit.yritys_id = laatija_yritys.yritys_id and
       audit.laatija_id = laatija_yritys.laatija_id
  left join kayttaja modifier on modifier.id = audit.modifiedby_id
where laatija_yritys.laatija_id = :id
order by laatija_yritys.yritys_id, audit.modifytime desc, audit.event_id desc

-- name: insert-laatija-yritys!
insert into laatija_yritys (laatija_id, yritys_id)
values (:laatija-id, :yritys-id)
on conflict (laatija_id, yritys_id) do update set tila_id = 0

-- name: delete-laatija-yritys!
update laatija_yritys set tila_id = 2
where laatija_id = :laatija-id and yritys_id = :yritys-id

-- name: select-laatija-laskutusosoitteet
select
  -1 id, null ytunnus, k.etunimi || ' ' || k.sukunimi nimi,
  l.vastaanottajan_tarkenne, l.jakeluosoite,
  l.postinumero, l.postitoimipaikka, l.maa,
  null verkkolaskuoperaattori, null verkkolaskuosoite,
  true as valid
from laatija l inner join kayttaja k on l.id = k.id
where l.id = :id
union all
select
  y.id, y.ytunnus, y.nimi,
  y.vastaanottajan_tarkenne, y.jakeluosoite,
  y.postinumero, y.postitoimipaikka, y.maa,
  y.verkkolaskuoperaattori, y.verkkolaskuosoite,
  not y.deleted and laatija_yritys.tila_id = 1 as valid
from yritys y inner join laatija_yritys
  on laatija_yritys.laatija_id = :id and
     laatija_yritys.yritys_id = y.id;

-- name: select-count-public-laatijat
select
  count(*)
from
  laatija inner join kayttaja on laatija.id = kayttaja.id
where
  not partner and
  kayttaja.login is not null and
  patevyys_voimassa(laatija) and
  not laatija.laatimiskielto;

-- name: select-laatija-history
select
  l.id, l.patevyystaso,
  l.toteamispaivamaara, l.toteaja, l.laatimiskielto, l.partner,
  l.toimintaalue, l.muut_toimintaalueet as muuttoimintaalueet,
  l.julkinen_puhelin as julkinenpuhelin,
  l.julkinen_email as julkinenemail,
  l.julkinen_osoite as julkinenosoite,
  l.julkinen_wwwosoite as julkinenwwwosoite,
  l.julkinen_postinumero as julkinenpostinumero,
  l.laskutuskieli,
  l.vastaanottajan_tarkenne, l.jakeluosoite,
  l.postinumero, l.postitoimipaikka, l.wwwosoite, l.maa,
  l.modifytime,
  fullname(modifier) modifiedby_name
from
  audit.laatija l
  join kayttaja modifier on l.modifiedby_id = modifier.id
where l.id = :id
order by modifytime, event_id;

-- name: insert-patevyys-expiration-viestit
with laatija_patevyys as (
  select
    kayttaja.id laatija_id, kayttaja.email,
    date_trunc('day', patevyys_paattymisaika(laatija))
      - :months-before-expiration * interval '1' month as begin,
    date_trunc('day', patevyys_paattymisaika(laatija))
      - :months-before-expiration * interval '1' month
      + :fallback-window * interval '1' day as end
  from kayttaja inner join laatija on laatija.id = kayttaja.id
), laatija_expiration_viesti as (
  select row_number() over (order by laatija_id) row_id, laatija_patevyys.*
  from laatija_patevyys
  where date_trunc('day', transaction_timestamp()) between laatija_patevyys.begin and laatija_patevyys.end
    and not exists (
      select from viesti inner join vastaanottaja
        on viesti.viestiketju_id = vastaanottaja.viestiketju_id
      where
        vastaanottaja.vastaanottaja_id = laatija_patevyys.laatija_id and
        viesti.from_id = -3 and
        date_trunc('day', viesti.sent_time) between laatija_patevyys.begin and laatija_patevyys.end
    )
), viestiketjut as (
  insert into viestiketju (subject, kasitelty)
  select :subject, true from laatija_expiration_viesti
  returning id
), laatija_viestiketju as (
  select laatija.*, viestiketju.id viestiketju_id
  from (
    (select row_number() over (order by id) row_id, id from viestiketjut) viestiketju
    inner join laatija_expiration_viesti laatija using (row_id))
), vastaanottajat as (
  insert into vastaanottaja (viestiketju_id, vastaanottaja_id)
  select viestiketju_id, laatija_id from laatija_viestiketju
), viestit as (
  insert into viesti (viestiketju_id, body)
  select viestiketju_id, :body
  from laatija_viestiketju
)
select laatija_id as id, email
from laatija_viestiketju
