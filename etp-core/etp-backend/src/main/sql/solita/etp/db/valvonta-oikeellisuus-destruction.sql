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

-- name: select-linked-liitteet-ids
select liite.id
from vo_toimenpide inner join liite on liite.vo_toimenpide_id = vo_toimenpide.id
where vo_toimenpide.id = :vo_toimenpide_id;

-- name: destroy-vo-virhe-table-data!
delete from vo_virhe where toimenpide_id = :vo-toimenpide-id;
