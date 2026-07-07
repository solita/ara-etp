alter table Energiatodistus
    add column is$hiilikadenjalki$rakennus$hyodyntaminen_energiana numeric,
    add column is$hiilikadenjalki$rakennuspaikka$hyodyntaminen_energiana numeric;

alter table audit.energiatodistus
    add column is$hiilikadenjalki$rakennus$hyodyntaminen_energiana numeric,
    add column is$hiilikadenjalki$rakennuspaikka$hyodyntaminen_energiana numeric;

call audit.create_audit_procedure('energiatodistus'::name);
