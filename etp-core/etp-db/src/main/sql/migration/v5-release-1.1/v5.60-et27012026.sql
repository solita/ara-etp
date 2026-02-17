alter table energiatodistus
    drop column h$iv_ilmastointi$asetukset_tehostettavissa,
    drop column h$iv_ilmastointi$kayttoikaa_jaljella_arvio_vuosina,
    drop column h$lammitys$asetukset_tehostettavissa;

alter table energiatodistus
    add column lt$energiankulutuksen_valmius_reagoida_ulkoisiin_signaaleihin boolean not null default false,
    add column lt$lammitys$lammonjako_lampotilajousto boolean not null default false,

    add column to$tietojen_alkuperavuosi integer,
    add column to$lisatietoja_fi text,
    add column to$lisatietoja_sv text;

alter type toimenpide
    add attribute kasvihuonepaastojen_muutos numeric;

