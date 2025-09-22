-- name: select-for-ppp-add-requirements
select
    energiatodistus.laatija_id,
    energiatodistus.tila_id,
    energiatodistus.versio
from energiatodistus
where id = :energiatodistus-id;

-- name: select-perusparannuspassi
select
    ppp.*,
    et.laatija_id,
    et.tila_id
from perusparannuspassi ppp
    join energiatodistus et on ppp.energiatodistus_id = et.id
where ppp.id = :id and et.laatija_id = :laatija-id;

-- name: select-perusparannuspassi-vaiheet
select *
from perusparannuspassi_vaihe vaihe
where perusparannuspassi_id = :perusparannuspassi-id
order by vaihe_nro asc;

-- name: update-vaihe!
update perusparannuspassi_vaihe
set
    valid = :valid
where
    vaihe_nro = :vaihe-nro and
    perusparannuspassi_id = :perusparannuspassi-id;
