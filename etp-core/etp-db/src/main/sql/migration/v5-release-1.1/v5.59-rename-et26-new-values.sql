alter table energiatodistus
    rename column h$lammitys_kayttoikaa_jaljella_arvio_vuosina to h$lammitys$kayttoikaa_jaljella_arvio_vuosina;
alter table energiatodistus
    rename column h$lammitys_asetukset_tehostettavissa to h$lammitys$asetukset_tehostettavissa;
alter table energiatodistus
    rename column h$iv_ilmastointi_kayttoikaa_jaljella_arvio_vuosina to h$iv_ilmastointi$kayttoikaa_jaljella_arvio_vuosina;
alter table energiatodistus
    rename column h$iv_ilmastointi_asetukset_tehostettavissa to h$iv_ilmastointi$asetukset_tehostettavissa;

alter table energiatodistus
    rename column t$uusiutuvat_omavaraisenergiat$aurinkolampo_kokonaistuotanto to t$uusiutuvat_omavaraisenergiat$kokonaistuotanto$aurinkolampo;
alter table energiatodistus
    rename column t$uusiutuvat_omavaraisenergiat$aurinkosahko_kokonaistuotanto to t$uusiutuvat_omavaraisenergiat$kokonaistuotanto$aurinkosahko;
alter table energiatodistus
    rename column t$uusiutuvat_omavaraisenergiat$tuulisahko_kokonaistuotanto to t$uusiutuvat_omavaraisenergiat$kokonaistuotanto$tuulisahko;
alter table energiatodistus
    rename column t$uusiutuvat_omavaraisenergiat$lampopumppu_kokonaistuotanto to t$uusiutuvat_omavaraisenergiat$kokonaistuotanto$lampopumppu;
alter table energiatodistus
    rename column t$uusiutuvat_omavaraisenergiat$muulampo_kokonaistuotanto to t$uusiutuvat_omavaraisenergiat$kokonaistuotanto$muulampo;
alter table energiatodistus
    rename column t$uusiutuvat_omavaraisenergiat$muusahko_kokonaistuotanto to t$uusiutuvat_omavaraisenergiat$kokonaistuotanto$muusahko;
