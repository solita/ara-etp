
-- name: select-all-viestiketjut
select
  viestiketju.id, viestiketju.kasittelija_id, viestiketju.kasitelty, viestiketju.subject,
  viestiketju.vastaanottajaryhma_id, viestiketju.energiatodistus_id, viestiketju.vo_toimenpide_id,
  vastaanottajat.ids vastaanottajat
  from viestiketju
  left join lateral (
    select array_agg(vastaanottaja_id) ids from vastaanottaja
    where vastaanottaja.viestiketju_id = viestiketju.id
  ) vastaanottajat on true
where
  (:from-id::int is null or viestiketju.from_id = :from-id) and
  (:vastaanottaja-id::int is null or :vastaanottaja-id =ANY(vastaanottajat.ids)) and
  (:valvonta::bool is null or (viestiketju.energiatodistus_id is not null) = :valvonta) and
  (:keyword::text is null or viestiketju.subject ilike :keyword or exists (
    select from viesti where viesti.body ilike :keyword and viesti.viestiketju_id = viestiketju.id)) and
  (not viestiketju.kasitelty or :include-kasitelty) and
  ((viestiketju.kasittelija_id = :kasittelija-id or
   (viestiketju.kasittelija_id is not null) = :has-kasittelija) or
   (:kasittelija-id::int is null and :has-kasittelija::boolean is null))
order by (select max(sent_time) from viesti where viestiketju_id = viestiketju.id) desc
limit :limit offset :offset;

-- name: select-viestiketjut-for-kayttaja
select
  viestiketju.id, viestiketju.kasittelija_id, viestiketju.kasitelty, viestiketju.subject,
  viestiketju.vastaanottajaryhma_id, viestiketju.energiatodistus_id, viestiketju.vo_toimenpide_id,
  (select array_agg(vastaanottaja_id) from vastaanottaja
    where vastaanottaja.viestiketju_id = viestiketju.id) vastaanottajat
from viestiketju
where from_id = :kayttaja-id or vastaanottajaryhma_id = :vastaanottajaryhma-id or
      exists (
        select 1 from vastaanottaja
        where vastaanottaja.vastaanottaja_id = :kayttaja-id and
              vastaanottaja.viestiketju_id = viestiketju.id
      )
order by (select max(sent_time) from viesti where viestiketju_id = viestiketju.id) desc
limit :limit offset :offset;

-- name: select-count-all-viestiketjut
select count(*) count from viestiketju
  left join lateral (
    select array_agg(vastaanottaja_id) ids from vastaanottaja
    where vastaanottaja.viestiketju_id = viestiketju.id
  ) vastaanottajat on true
where
  (:from-id::int is null or viestiketju.from_id = :from-id) and
  (:vastaanottaja-id::int is null or :vastaanottaja-id =ANY(vastaanottajat.ids)) and
  (:valvonta::bool is null or (viestiketju.energiatodistus_id is not null) = :valvonta) and
  (:keyword::text is null or viestiketju.subject ilike :keyword or exists (
    select from viesti where viesti.body ilike :keyword and viesti.viestiketju_id = viestiketju.id)) and
  (not viestiketju.kasitelty or :include-kasitelty) and
  ((viestiketju.kasittelija_id = :kasittelija-id or
    (viestiketju.kasittelija_id is not null) = :has-kasittelija) or
   (:kasittelija-id::int is null and :has-kasittelija::boolean is null));

-- name: select-count-viestiketjut-for-kayttaja
select count(*) count
from viestiketju
where from_id = :kayttaja-id or vastaanottajaryhma_id = :vastaanottajaryhma-id or
  exists (
    select 1 from vastaanottaja
    where vastaanottaja.vastaanottaja_id = :kayttaja-id and
        vastaanottaja.viestiketju_id = viestiketju.id
  );

-- name: select-viestiketju
select
  viestiketju.id, viestiketju.kasittelija_id, viestiketju.kasitelty, viestiketju.subject,
  viestiketju.vastaanottajaryhma_id, viestiketju.energiatodistus_id, viestiketju.vo_toimenpide_id,
  (select array_agg(vastaanottaja_id) from vastaanottaja
    where vastaanottaja.viestiketju_id = viestiketju.id) vastaanottajat
from viestiketju
where id = :id;

-- name: select-viestit
select
  viesti.sent_time, viesti.body,
  kayttaja.id       from$id,
  kayttaja.rooli_id from$rooli_id,
  kayttaja.etunimi  from$etunimi,
  kayttaja.sukunimi from$sukunimi,
  viesti_reader.read_time
from viesti
  inner join kayttaja on kayttaja.id = viesti.from_id
  left join viesti_reader
    on viesti_reader.viesti_id = viesti.id and
       viesti_reader.reader_id = :reader-id
where viesti.viestiketju_id = :id
 order by viesti.sent_time asc;

-- name: select-kayttajat
select id, etunimi, sukunimi, rooli_id from kayttaja;

-- name: select-kasittelijat
select id, etunimi, sukunimi, rooli_id from kayttaja WHERE rooli_id IN (2, 3);

--name: read-ketju!
insert into viesti_reader (viesti_id)
select viesti.id from viesti where viesti.viestiketju_id = :viestiketju-id
on conflict (viesti_id, reader_id) do nothing

-- name: select-count-unread-for-kasittelija
select count(*)
from viestiketju
where (kasittelija_id is null or kasittelija_id = :kayttaja-id) and
  not kasitelty and exists(
  select from viesti where viesti.viestiketju_id = viestiketju.id and not exists(
    select from viesti_reader where viesti_reader.viesti_id = viesti.id and
                                    viesti_reader.reader_id = :kayttaja-id
));

-- name: select-count-unread-for-kayttaja
select count(*)
from viestiketju
where (from_id = :kayttaja-id or vastaanottajaryhma_id = :vastaanottajaryhma-id or
  exists (
    select 1 from vastaanottaja
    where vastaanottaja.vastaanottaja_id = :kayttaja-id and
        vastaanottaja.viestiketju_id = viestiketju.id
    )) and exists(
  select from viesti where viesti.viestiketju_id = viestiketju.id and not exists(
    select from viesti_reader where viesti_reader.viesti_id = viesti.id and
        viesti_reader.reader_id = :kayttaja-id
    ));

-- name: select-energiatodistus-viestiketjut
select
  viestiketju.id, viestiketju.kasittelija_id, viestiketju.kasitelty, viestiketju.subject,
  viestiketju.vastaanottajaryhma_id, viestiketju.energiatodistus_id, viestiketju.vo_toimenpide_id,
  (select array_agg(vastaanottaja_id) from vastaanottaja
   where vastaanottaja.viestiketju_id = viestiketju.id) vastaanottajat
from viestiketju
where viestiketju.energiatodistus_id = :energiatodistus-id
order by (select max(sent_time) from viesti where viestiketju_id = viestiketju.id) desc

-- name: select-liite
select nimi, viestiketju_id, contenttype, nimi as filename, deleted
from viesti_liite
where id = :id;

-- name: select-liite-by-viestiketju-id
select distinct on (l.id) l.id, a.modifytime createtime,
  fullname(k.*) "author-fullname", l.nimi, l.contenttype, l.url,
  l.deleted
from viesti_liite l
     inner join audit.viesti_liite a on l.id = a.id
     inner join kayttaja k on a.modifiedby_id = k.id
where l.viestiketju_id = :viestiketju-id
order by l.id, a.modifytime asc, a.event_id asc;

-- name: delete-liite!
update viesti_liite set deleted = true where id = :id;

-- name: select-owner
select modifiedby_id from audit.viesti_liite where id = :id
order by modifytime asc, event_id asc
limit 1;