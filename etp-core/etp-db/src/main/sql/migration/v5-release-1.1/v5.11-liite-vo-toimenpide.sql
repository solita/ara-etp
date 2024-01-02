
alter table etp.liite add column
  vo_toimenpide_id int references etp.vo_toimenpide (id);

alter table audit.liite add column
  vo_toimenpide_id int references etp.vo_toimenpide (id);

call audit.create_audit_procedure('liite'::name);