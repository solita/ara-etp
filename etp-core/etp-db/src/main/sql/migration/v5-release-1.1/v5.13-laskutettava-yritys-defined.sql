alter table etp.energiatodistus
  add column laskutettava_yritys_defined boolean not null default true;

alter table audit.energiatodistus
  add column laskutettava_yritys_defined boolean not null default true;
call audit.create_audit_procedure('energiatodistus'::name);

alter table etp.energiatodistus
  alter column laskutettava_yritys_defined set default false;

alter table audit.energiatodistus
  alter column laskutettava_yritys_defined set default false;
