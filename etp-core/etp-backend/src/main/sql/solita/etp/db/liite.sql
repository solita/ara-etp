
-- name: insert-liite<!
insert into liite (data) values (:data :: JSONB)
returning id

-- name: select-liite
select liite.nimi, liite.energiatodistus_id, liite.contenttype from liite
where id = :id and deleted = false

-- name: select-liite-by-energiatodistus-id
select distinct on (l.id) l.id, a.modifytime createtime,
  fullname(k.*) "author-fullname",
  l.nimi, l.contenttype, l.url, l.deleted
from liite l
inner join audit.liite a on l.id = a.id
inner join kayttaja k on a.modifiedby_id = k.id
where l.energiatodistus_id = :energiatodistus-id and l.deleted = false
order by l.id, a.modifytime asc, a.event_id asc

-- name: delete-liite!
update liite set deleted = true where id = :id
