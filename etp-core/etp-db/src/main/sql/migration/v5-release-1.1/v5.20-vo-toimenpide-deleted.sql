alter table vo_toimenpide add column deleted boolean not null default false;
alter table audit.vo_toimenpide
  add column deleted boolean not null default false;
call audit.create_audit_procedure('vo_toimenpide'::name);