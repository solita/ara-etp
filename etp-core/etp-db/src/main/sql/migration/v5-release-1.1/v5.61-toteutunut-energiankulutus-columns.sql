-- Bundled in this migration are some updates to the audit.energiatodistus table
-- to match changes made in v5.55-v5.60
alter table audit.energiatodistus
  add column pt$havainnointikayntityyppi_id integer,
  add column h$lammitys$kayttoikaa_jaljella_arvio_vuosina int,
  add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkosahko numeric,
  add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$aurinkolampo numeric,
  add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$tuulisahko numeric,
  add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$lampopumppu numeric,
  add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$muulampo numeric,
  add column t$uusiutuvat_omavaraisenergiat_kokonaistuotanto$muusahko numeric,
  add column lt$energiankulutuksen_valmius_reagoida_ulkoisiin_signaaleihin boolean,
  add column lt$lammitys$lammonjako_lampotilajousto boolean,
  add column to$tietojen_alkuperavuosi integer,
  add column to$lisatietoja_fi text,
  add column to$lisatietoja_sv text;

alter table energiatodistus
  add column to$uusiutuvat_polttoaineet_vuosikulutus_yhteensa numeric,
  add column to$fossiiliset_polttoaineet_vuosikulutus_yhteensa numeric,
  add column to$tuotettu_uusiutuva_energia numeric;

alter table audit.energiatodistus
  add column to$uusiutuvat_polttoaineet_vuosikulutus_yhteensa numeric,
  add column to$fossiiliset_polttoaineet_vuosikulutus_yhteensa numeric,
  add column to$tuotettu_uusiutuva_energia numeric;

call audit.create_audit_procedure('energiatodistus'::name);
