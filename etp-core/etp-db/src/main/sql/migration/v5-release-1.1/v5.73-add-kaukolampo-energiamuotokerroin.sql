-- AE-2777: Changeable district heating energy form coefficient (ET2027).
--
-- null means that the laatija has not changed the coefficient and the fixed
-- coefficient of the system is used. The value is allowed only in ET2027 and
-- must be within [0.2, 0.38].
alter table energiatodistus
    add column t$kaytettavat_energiamuodot$kaukolampo_kerroin numeric
        constraint energiatodistus_kaukolampo_kerroin_check
            check (t$kaytettavat_energiamuodot$kaukolampo_kerroin is null
                   or (versio = 2027
                       and t$kaytettavat_energiamuodot$kaukolampo_kerroin between 0.2 and 0.38));

alter table audit.energiatodistus
    add column t$kaytettavat_energiamuodot$kaukolampo_kerroin numeric;

-- Regenerate the audit procedure to include the new column
call audit.create_audit_procedure('energiatodistus'::name);

