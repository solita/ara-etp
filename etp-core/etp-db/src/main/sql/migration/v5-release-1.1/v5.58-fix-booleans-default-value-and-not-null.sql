update energiatodistus
    set h$lammitys_asetukset_tehostettavissa = false
    where h$lammitys_asetukset_tehostettavissa is null;

update energiatodistus
    set h$iv_ilmastointi_asetukset_tehostettavissa = false
    where h$iv_ilmastointi_asetukset_tehostettavissa is null;

alter table energiatodistus
    alter column h$lammitys_asetukset_tehostettavissa set default false,
    alter column h$lammitys_asetukset_tehostettavissa set not null,
    alter column h$iv_ilmastointi_asetukset_tehostettavissa set default false,
    alter column h$iv_ilmastointi_asetukset_tehostettavissa set not null;
