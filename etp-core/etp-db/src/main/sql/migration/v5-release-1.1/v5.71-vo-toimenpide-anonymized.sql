alter table vo_toimenpide add column anonymized boolean not null default false;
alter table audit.vo_toimenpide
  add column anonymized boolean not null default false;
call audit.create_audit_procedure('vo_toimenpide'::name);

-- A foreign key cannot have a condition, so expose the id of non-anonymized
-- rows as a separate unique key (null when anonymized) and point the foreign
-- keys at it instead of at vo_toimenpide.id. This prevents linking new rows
-- to an anonymized vo_toimenpide, and prevents anonymizing a vo_toimenpide
-- that still has linked rows.
alter table vo_toimenpide
  add column non_anonymized_id int
    generated always as (case when anonymized then null else id end) stored,
  add constraint vo_toimenpide_non_anonymized_id_key unique (non_anonymized_id);
alter table vo_virhe
  drop constraint vo_virhe_toimenpide_id_fkey,
  add constraint vo_virhe_toimenpide_id_fkey
    foreign key (toimenpide_id) references vo_toimenpide (non_anonymized_id);
alter table vo_tiedoksi
  drop constraint vo_tiedoksi_toimenpide_id_fkey,
  add constraint vo_tiedoksi_toimenpide_id_fkey
    foreign key (toimenpide_id) references vo_toimenpide (non_anonymized_id);
alter table viestiketju
  drop constraint viestiketju_vo_toimenpide_id_fkey,
  add constraint viestiketju_vo_toimenpide_id_fkey
    foreign key (vo_toimenpide_id) references vo_toimenpide (non_anonymized_id);
alter table liite
  drop constraint liite_vo_toimenpide_id_fkey,
  add constraint liite_vo_toimenpide_id_fkey
    foreign key (vo_toimenpide_id) references vo_toimenpide (non_anonymized_id);
