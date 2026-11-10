alter table energiatodistus
    add column t$kaytettavat_energiamuodot_kertoimet$kaukolampo numeric;

alter table audit.energiatodistus
    add column t$kaytettavat_energiamuodot_kertoimet$kaukolampo numeric;

call audit.create_audit_procedure('energiatodistus'::name);
