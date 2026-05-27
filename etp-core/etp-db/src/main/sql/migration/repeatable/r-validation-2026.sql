
insert into validation_required_column (versio, column_name, ordinal, bypass_allowed)
values
(2026, 'pt$kieli', 0, false),
(2026, 'pt$laatimisvaihe', 1, false),
(2026, 'pt$havainnointikaynti', 2, false),
(2026, 't$laskentatyokalu', 3, false),

(2026, 'pt$nimi_fi', 4, false),
(2026, 'pt$nimi_sv', 5, false),
(2026, 'pt$valmistumisvuosi', 6, false),
(2026, 'pt$katuosoite_fi', 7, false),
(2026, 'pt$katuosoite_sv', 8, false),
(2026, 'pt$postinumero', 9, false),
(2026, 'pt$rakennustunnus', 10, true),
(2026, 'pt$kayttotarkoitus', 11, false),
(2026, 'pt$keskeiset_suositukset_fi', 12, false),
(2026, 'pt$keskeiset_suositukset_sv', 13, false),

(2026, 'lt$lammitetty_nettoala', 14, false),
(2026, 'lt$rakennusvaippa$ilmanvuotoluku', 15, false),
(2026, 'lt$rakennusvaippa$lampokapasiteetti', 16, false),
(2026, 'lt$rakennusvaippa$ilmatilavuus', 17, false),
(2026, 'lt$rakennusvaippa$ulkoseinat$ala', 18, false),
(2026, 'lt$rakennusvaippa$ylapohja$ala', 19, false),
(2026, 'lt$rakennusvaippa$alapohja$ala', 20, false),
(2026, 'lt$rakennusvaippa$ikkunat$ala', 21, false),
(2026, 'lt$rakennusvaippa$ulkoovet$ala', 22, false),
(2026, 'lt$rakennusvaippa$ulkoseinat$U', 23, false),
(2026, 'lt$rakennusvaippa$ylapohja$U', 24, false),
(2026, 'lt$rakennusvaippa$alapohja$U', 25, false),
(2026, 'lt$rakennusvaippa$ikkunat$U', 26, false),
(2026, 'lt$rakennusvaippa$ulkoovet$U', 27, false),
(2026, 'lt$rakennusvaippa$kylmasillat_UA', 28, false),

(2026, 'lt$ikkunat$etela$U', 29, false),
(2026, 'lt$ikkunat$etela$g_ks', 30, false),
(2026, 'lt$ikkunat$ita$U', 31, false),
(2026, 'lt$ikkunat$ita$g_ks', 32, false),
(2026, 'lt$ikkunat$kaakko$U', 33, false),
(2026, 'lt$ikkunat$kaakko$g_ks', 34, false),
(2026, 'lt$ikkunat$koillinen$U', 35, false),
(2026, 'lt$ikkunat$koillinen$g_ks', 36, false),
(2026, 'lt$ikkunat$lansi$U', 37, false),
(2026, 'lt$ikkunat$lansi$g_ks', 38, false),
(2026, 'lt$ikkunat$lounas$U', 39, false),
(2026, 'lt$ikkunat$lounas$g_ks', 40, false),
(2026, 'lt$ikkunat$luode$U', 41, false),
(2026, 'lt$ikkunat$luode$g_ks', 42, false),
(2026, 'lt$ikkunat$pohjoinen$U', 43, false),
(2026, 'lt$ikkunat$pohjoinen$g_ks', 44, false),

(2026, 'lt$ilmanvaihto$tyyppi_id', 45, false),
(2026, 'lt$ilmanvaihto$kuvaus_fi', 46, false),
(2026, 'lt$ilmanvaihto$kuvaus_sv', 47, false),
(2026, 'lt$ilmanvaihto$paaiv$tulo', 48, false),
(2026, 'lt$ilmanvaihto$paaiv$poisto', 49, false),
(2026, 'lt$ilmanvaihto$erillispoistot$tulo', 50, false),
(2026, 'lt$ilmanvaihto$erillispoistot$poisto', 51, false),
(2026, 'lt$ilmanvaihto$ivjarjestelma$tulo', 52, false),
(2026, 'lt$ilmanvaihto$ivjarjestelma$poisto', 53, false),
(2026, 'lt$ilmanvaihto$ivjarjestelma$sfp', 54, false),
(2026, 'lt$ilmanvaihto$lto_vuosihyotysuhde', 55, false),

(2026, 'lt$lammitys$lammitysmuoto_1$id', 56, false),
(2026, 'lt$lammitys$lammitysmuoto_1$kuvaus_fi', 57, false),
(2026, 'lt$lammitys$lammitysmuoto_1$kuvaus_sv', 58, false),
(2026, 'lt$lammitys$lammitysmuoto_2$kuvaus_fi', 59, false),
(2026, 'lt$lammitys$lammitysmuoto_2$kuvaus_sv', 60, false),
(2026, 'lt$lammitys$lammonjako$id', 61, false),
(2026, 'lt$lammitys$lammonjako$kuvaus_fi', 62, false),
(2026, 'lt$lammitys$lammonjako$kuvaus_sv', 63, false),
(2026, 'lt$lammitys$tilat_ja_iv$jaon_hyotysuhde', 64, false),
(2026, 'lt$lammitys$tilat_ja_iv$apulaitteet', 65, false),
(2026, 'lt$lammitys$lammin_kayttovesi$jaon_hyotysuhde', 66, false),

(2026, 'lt$lkvn_kaytto$ominaiskulutus', 67, false),
(2026, 'lt$lkvn_kaytto$lammitysenergian_nettotarve', 68, false),

(2026, 'h$ymparys$teksti_fi', 69, false),
(2026, 'h$ymparys$teksti_sv', 70, false),

(2026, 'to$tietojen_alkuperavuosi', 71, false),

(2026, 'is$laatimisajankohta', 72, false)

on conflict (column_name, versio) do update set
  ordinal = excluded.ordinal,
  bypass_allowed = excluded.bypass_allowed;

update validation_required_column
set valid = false
where versio = 2026 and column_name in (
  'pt$nimi',
  'lt$ilmanvaihto$paaiv$tulo',
  'lt$ilmanvaihto$paaiv$poisto',
  'lt$ilmanvaihto$erillispoistot$tulo',
  'lt$ilmanvaihto$erillispoistot$poisto',
  'lt$ilmanvaihto$ivjarjestelma$tulo'
);

insert into validation_sisainen_kuorma (
  versio, kayttotarkoitusluokka_id,

  valaistus$kayttoaste,
  valaistus$lampokuorma,
  kuluttajalaitteet$kayttoaste,
  kuluttajalaitteet$lampokuorma,
  henkilot$kayttoaste,
  henkilot$lampokuorma)
values
(2026, 1, 0.1, 6, 0.6, 3, 0.6, 2),
(2026, 2, 0.1, 9, 0.6, 4, 0.6, 3),
(2026, 3, 0.65, 10, 0.65, 12, 0.65, 5),
(2026, 4, 1, 19, 1, 1, 1, 2),
(2026, 5, 0.3, 11, 0.3, 4, 0.3, 4),
(2026, 6, 0.6, 14, 0.6, 8, 0.6, 14),
(2026, 7, 0.5, 10, 0.5, 0, 0.5, 5),
(2026, 8, 0.6, 7, 0.6, 9, 0.6, 8)
on conflict (kayttotarkoitusluokka_id, versio) do update
  set valaistus$kayttoaste = excluded.valaistus$kayttoaste,
      valaistus$lampokuorma = excluded.valaistus$lampokuorma,
      kuluttajalaitteet$kayttoaste = excluded.kuluttajalaitteet$kayttoaste,
      kuluttajalaitteet$lampokuorma = excluded.kuluttajalaitteet$lampokuorma,
      henkilot$kayttoaste = excluded.henkilot$kayttoaste,
      henkilot$lampokuorma = excluded.henkilot$lampokuorma;
