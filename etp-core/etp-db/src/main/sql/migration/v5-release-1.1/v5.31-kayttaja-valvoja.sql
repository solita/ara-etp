alter table kayttaja add column valvoja boolean not null default false;
alter table audit.kayttaja add column valvoja boolean not null default false;
call audit.create_audit_procedure('kayttaja'::name);
