alter table laatija add column julkinen_postinumero BOOLEAN NOT NULL DEFAULT false;
alter table audit.laatija add column julkinen_postinumero BOOLEAN NOT NULL DEFAULT false;
call audit.create_audit_procedure('laatija'::name);
