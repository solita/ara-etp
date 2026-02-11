alter table energiatodistus
    drop column if exists h$iv_ilmastointi$asetukset_tehostettavissa,
    drop column if exists h$iv_ilmastointi$kayttoikaa_jaljella_arvio_vuosina,
    drop column if exists h$lammitys$asetukset_tehostettavissa;

alter table energiatodistus
    add column if not exists lt$rakennus_kykenee_reagoimaan_ulkoisiin_signaaleihin boolean not null default false,
    add column if not exists lt$lammitys$lammonjakojarjestelma_mukautettavissa boolean not null default false,

    add column if not exists to$tietojen_alkuperavuosi integer,
    add column if not exists to$lisatietoja_fi text,
    add column if not exists to$lisatietoja_sv text;
