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
where ppp.id = :id and et.laatija_id = :laatija-id and ppp.valid = true;

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

-- name: select-perusparannuspassi-vaihe-toimenpide-ehdotukset
select
    pvte.toimenpide_ehdotus_id as id
from perusparannuspassi_vaihe_toimenpide_ehdotus pvte
where perusparannuspassi_id = :perusparannuspassi-id
  and vaihe_nro = :vaihe-nro
order by pvte.ordinal asc, pvte.toimenpide_ehdotus_id asc;

-- name: delete-perusparannuspassi-vaihe-toimenpide-ehdotukset!
delete
from perusparannuspassi_vaihe_toimenpide_ehdotus
where perusparannuspassi_id = :perusparannuspassi-id
  and vaihe_nro = :vaihe-nro;

-- name: find-by-energiatodistus-id
select
    ppp.*,
    et.laatija_id,
    et.tila_id
from perusparannuspassi ppp
         join energiatodistus et on ppp.energiatodistus_id = et.id
where ppp.energiatodistus_id = :energiatodistus-id
  and et.laatija_id = :laatija-id
  and ppp.valid = true;

-- name: delete-perusparannuspassi!
update perusparannuspassi set
    valid = false
where id = :id;

-- name: delete-perusparannuspassi-vaiheet!
update perusparannuspassi_vaihe
set
    valid = false
where
    perusparannuspassi_id = :perusparannuspassi-id;

-- name: find-deleted-by-energiatodistus-id
select
    ppp.id,
    ppp.energiatodistus_id
from perusparannuspassi ppp
         join energiatodistus et on ppp.energiatodistus_id = et.id
where ppp.energiatodistus_id = :energiatodistus-id
  and et.laatija_id = :laatija-id
  and ppp.valid = false;

-- name: resurrect-perusparannuspassi!
update perusparannuspassi
set
    valid = true
where id = :id;

-- name: resurrect-perusparannuspassi-vaiheet!
update perusparannuspassi_vaihe
set
    valid = true
where
    perusparannuspassi_id = :perusparannuspassi-id;
