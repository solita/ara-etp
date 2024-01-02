
alter table etp.viestiketju add
  vo_toimenpide_id int references vo_toimenpide (id);

alter table audit.viestiketju add column vo_toimenpide_id int;
call audit.create_audit_procedure('viestiketju'::name);