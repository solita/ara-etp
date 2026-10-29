-- name: select-expired-oikeellisuuden-valvonta-ids
with toimenpiteet_with_latest_publish_time as (
    select
        id,
        energiatodistus_id,
        max(publish_time) over (
            partition by energiatodistus_id
        ) as latest_publish_time
    from vo_toimenpide
)
select id as "vo-toimenpide-id", energiatodistus_id as "energiatodistus-id"
from toimenpiteet_with_latest_publish_time
where latest_publish_time < (
    (now() at time zone 'Europe/Helsinki')::date - interval '2 years');

-- name: select-linked-liite-ids
select id as "liite-id", contenttype
from liite
where vo_toimenpide_id = :vo-toimenpide-id;

-- name: select-linked-viesti-liite-ids
select viesti_liite.id as "viesti-liite-id", viesti_liite.contenttype, viestiketju.id as "viestiketju-id"
from viesti_liite
    inner join viestiketju on viesti_liite.viestiketju_id = viestiketju.id
where viestiketju.vo_toimenpide_id = :vo-toimenpide-id;

-- name: select-linked-vo-note-ids
select id as "vo-note-id"
from vo_note
where energiatodistus_id = :energiatodistus-id
  and deleted = false;

-- name: destroy-vo-note-table-data!
delete from vo_note where id = :vo-note-id;

-- name: destroy-audit-vo-note-table-data!
delete from audit.vo_note where id = :vo-note-id;

-- name: destroy-vo-virhe-table-data!
delete from vo_virhe where toimenpide_id = :vo-toimenpide-id;

-- name: destroy-liite-table-data!
delete from liite where vo_toimenpide_id = :vo-toimenpide-id;

-- name: destroy-audit-liite-table-data!
delete from audit.liite where vo_toimenpide_id = :vo-toimenpide-id;

-- name: destroy-vo-tiedoksi-table-data!
delete from vo_tiedoksi where toimenpide_id = :vo-toimenpide-id;

-- name: destroy-vastaanottaja-table-data!
delete from vastaanottaja
where viestiketju_id in (
    select id
    from viestiketju
    where vo_toimenpide_id = :vo-toimenpide-id
);

-- name: destroy-viesti-reader-table-data!
delete from viesti_reader
where viesti_id in (
    select id
    from viesti
    where viestiketju_id in (
        select id
        from viestiketju
        where vo_toimenpide_id = :vo-toimenpide-id
    )
);

-- name: destroy-viesti-table-data!
delete from viesti
where viestiketju_id in (
    select id
    from viestiketju
    where vo_toimenpide_id = :vo-toimenpide-id
);

-- name: destroy-viesti-liite-table-data!
delete from viesti_liite
where viestiketju_id in (
    select id
    from viestiketju
    where vo_toimenpide_id = :vo-toimenpide-id
);

-- name: destroy-audit-viesti-liite-table-data!
delete from audit.viesti_liite
where viestiketju_id in (
    select id
    from viestiketju
    where vo_toimenpide_id = :vo-toimenpide-id
);

-- name: destroy-viestiketju-table-data!
delete from viestiketju where vo_toimenpide_id = :vo-toimenpide-id;

-- name: destroy-audit-viestiketju-table-data!
delete from audit.viestiketju where vo_toimenpide_id = :vo-toimenpide-id;

-- name: destroy-audit-vo-toimenpide-table-data!
delete from audit.vo_toimenpide where id = :vo-toimenpide-id;

-- name: anonymize-vo-toimenpide-table-data!
update vo_toimenpide
set
    -- id = null,
    -- type_id = null,
    -- author_id = null,
    -- energiatodistus_id = null,
    -- create_time = null,
    publish_time = null,
    deadline_date = null,
    template_id = null,
    -- diaarinumero = null,
    description = null,
    severity_id = null,
    deleted = true
where id = :vo-toimenpide-id;


