alter table energiatodistus
    rename column h$lammitys_kayttoikaa_jaljella_arvio_vuosina to h$lammitys$kayttoikaa_jaljella_arvio_vuosina;
alter table energiatodistus
    rename column h$lammitys_asetukset_tehostettavissa to h$lammitys$asetukset_tehostettavissa;
alter table energiatodistus
    rename column h$iv_ilmastointi_kayttoikaa_jaljella_arvio_vuosina to h$iv_ilmastointi$kayttoikaa_jaljella_arvio_vuosina;
alter table energiatodistus
    rename column h$iv_ilmastointi_asetukset_tehostettavissa to h$iv_ilmastointi$asetukset_tehostettavissa;
