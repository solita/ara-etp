alter table yritys add column deleted boolean not null default false;
alter table audit.yritys
  add column deleted boolean not null default false;
call audit.create_audit_procedure('yritys'::name);