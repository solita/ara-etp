-- Add missing ET2026 columns to audit.energiatodistus
-- These columns were added to etp.energiatodistus in migrations v5.55–v5.62
-- but were not added to the audit table at that time.

-- From v5.55: havainnointikayntityyppi
alter table audit.energiatodistus
    add column pt$havainnointikayntityyppi_id integer;

-- From v5.56 + v5.59 rename (surviving column after v5.60 drops)
alter table audit.energiatodistus
    add column h$lammitys$kayttoikaa_jaljella_arvio_vuosina int;

-- From v5.57 + v5.59 rename: uusiutuvat omavaraisenergiat kokonaistuotanto
alter table audit.energiatodistus
    add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkosahko numeric,
    add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkolampo numeric,
    add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$tuulisahko numeric,
    add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$lampopumppu numeric,
    add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$muulampo numeric,
    add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$muusahko numeric;

-- From v5.60: new ET2026 fields
alter table audit.energiatodistus
    add column lt$energiankulutuksen_valmius_reagoida_ulkoisiin_signaaleihin boolean not null default false,
    add column lt$lammitys$lammonjako_lampotilajousto boolean not null default false,
    add column to$tietojen_alkuperavuosi integer,
    add column to$lisatietoja_fi text,
    add column to$lisatietoja_sv text;

-- From v5.61: toteutunut-ostoenergiankulutus summary fields
alter table audit.energiatodistus
    add column to$uusiutuvat_polttoaineet_vuosikulutus_yhteensa numeric,
    add column to$fossiiliset_polttoaineet_vuosikulutus_yhteensa numeric,
    add column to$uusiutuva_energia_vuosituotto_yhteensa numeric;

-- From v5.62: ilmastoselvitys fields
alter table audit.energiatodistus
    add column is$laatimisajankohta date,
    add column is$laatija text,
    add column is$yritys text,
    add column is$yritys_osoite text,
    add column is$yritys_postinumero text,
    add column is$yritys_postitoimipaikka text,
    add column is$laadintaperuste integer,

    add column is$hiilijalanjalki$rakennus$rakennustuotteiden_valmistus numeric,
    add column is$hiilijalanjalki$rakennus$kuljetukset_tyomaavaihe numeric,
    add column is$hiilijalanjalki$rakennus$rakennustuotteiden_vaihdot numeric,
    add column is$hiilijalanjalki$rakennus$energiankaytto numeric,
    add column is$hiilijalanjalki$rakennus$purkuvaihe numeric,

    add column is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_valmistus numeric,
    add column is$hiilijalanjalki$rakennuspaikka$kuljetukset_tyomaavaihe numeric,
    add column is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_vaihdot numeric,
    add column is$hiilijalanjalki$rakennuspaikka$energiankaytto numeric,
    add column is$hiilijalanjalki$rakennuspaikka$purkuvaihe numeric,

    add column is$hiilikadenjalki$rakennus$uudelleenkaytto numeric,
    add column is$hiilikadenjalki$rakennus$kierratys numeric,
    add column is$hiilikadenjalki$rakennus$ylimaarainen_uusiutuvaenergia numeric,
    add column is$hiilikadenjalki$rakennus$hiilivarastovaikutus numeric,
    add column is$hiilikadenjalki$rakennus$karbonatisoituminen numeric,

    add column is$hiilikadenjalki$rakennuspaikka$uudelleenkaytto numeric,
    add column is$hiilikadenjalki$rakennuspaikka$kierratys numeric,
    add column is$hiilikadenjalki$rakennuspaikka$ylimaarainen_uusiutuvaenergia numeric,
    add column is$hiilikadenjalki$rakennuspaikka$hiilivarastovaikutus numeric,
    add column is$hiilikadenjalki$rakennuspaikka$karbonatisoituminen numeric;

-- Regenerate the audit procedure to include all new columns
call audit.create_audit_procedure('energiatodistus'::name);
