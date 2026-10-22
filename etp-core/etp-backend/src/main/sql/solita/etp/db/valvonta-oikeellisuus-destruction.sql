-- name: select-expired-oikeellisuuden-valvonta-ids
with toimenpiteet_with_latest_publish_time as (
    select
        id,
        max(publish_time) over (
            partition by energiatodistus_id, diaarinumero
        ) as latest_publish_time
    from vo_toimenpide
)
select id as "vo-toimenpide-id"
from toimenpiteet_with_latest_publish_time
where latest_publish_time < (
    (now() at time zone 'Europe/Helsinki')::date - interval '2 years');

-- name: select-linked-liite-ids
select id as "liite-id", contenttype
from liite
where vo_toimenpide_id = :vo-toimenpide-id;

-- name: destroy-vo-virhe-table-data!
delete from vo_virhe where toimenpide_id = :vo-toimenpide-id;

-- name: destroy-liite-table-data!
delete from liite where vo_toimenpide_id = :vo-toimenpide-id;

-- name: destroy-audit-liite-table-data!
delete from audit.liite where vo_toimenpide_id = :vo-toimenpide-id;
