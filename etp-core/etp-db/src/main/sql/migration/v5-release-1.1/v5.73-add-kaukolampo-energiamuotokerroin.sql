alter table energiatodistus
    add column t$kaytettavat_energiamuodot$kaukolampo_kerroin numeric;

alter table audit.energiatodistus
    add column t$kaytettavat_energiamuodot$kaukolampo_kerroin numeric;

call audit.create_audit_procedure('energiatodistus'::name);
