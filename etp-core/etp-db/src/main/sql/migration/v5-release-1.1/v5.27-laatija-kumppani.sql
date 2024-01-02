alter table laatija add column partner boolean not null default false;
alter table audit.laatija add column partner boolean not null default false;
call audit.create_audit_procedure('laatija'::name);
