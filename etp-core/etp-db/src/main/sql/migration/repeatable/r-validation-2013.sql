update validation_required_column
set valid = false
where versio = 2013 and column_name = 'pt$nimi';

insert into validation_required_column (versio, column_name, ordinal, bypass_allowed)
values
(2013, 'pt$kieli', 0, false),
(2013, 't$laskentatyokalu', 1, false),

(2013, 'pt$nimi_fi', 2, false),
(2013, 'pt$nimi_sv', 3, false),
(2013, 'pt$valmistumisvuosi', 4, false),
(2013, 'pt$katuosoite_fi', 5, false),
(2013, 'pt$katuosoite_sv', 6, false),
(2013, 'pt$postinumero', 7, false),
(2013, 'pt$rakennustunnus', 8, true),
(2013, 'pt$kayttotarkoitus', 9, false),

(2013, 'lt$lammitetty_nettoala', 10, false),
(2013, 'lt$rakennusvaippa$ilmanvuotoluku', 11, false),
(2013, 'lt$rakennusvaippa$ulkoseinat$ala', 12, false),
(2013, 'lt$rakennusvaippa$ylapohja$ala', 13, false),
(2013, 'lt$rakennusvaippa$alapohja$ala', 14, false),
(2013, 'lt$rakennusvaippa$ikkunat$ala', 15, false),
(2013, 'lt$rakennusvaippa$ulkoovet$ala', 16, false),
(2013, 'lt$rakennusvaippa$ulkoseinat$U', 17, false),
(2013, 'lt$rakennusvaippa$ylapohja$U', 18, false),
(2013, 'lt$rakennusvaippa$alapohja$U', 19, false),
(2013, 'lt$rakennusvaippa$ikkunat$U', 20, false),
(2013, 'lt$rakennusvaippa$ulkoovet$U', 21, false),
(2013, 'lt$rakennusvaippa$kylmasillat_UA', 22, false),

(2013, 'lt$ikkunat$etela$U', 23, false),
(2013, 'lt$ikkunat$etela$g_ks', 24, false),
(2013, 'lt$ikkunat$ita$U', 25, false),
(2013, 'lt$ikkunat$ita$g_ks', 26, false),
(2013, 'lt$ikkunat$kaakko$U', 27, false),
(2013, 'lt$ikkunat$kaakko$g_ks', 28, false),
(2013, 'lt$ikkunat$koillinen$U', 29, false),
(2013, 'lt$ikkunat$koillinen$g_ks', 30, false),
(2013, 'lt$ikkunat$lansi$U', 31, false),
(2013, 'lt$ikkunat$lansi$g_ks', 32, false),
(2013, 'lt$ikkunat$lounas$U', 33, false),
(2013, 'lt$ikkunat$lounas$g_ks', 34, false),
(2013, 'lt$ikkunat$luode$U', 35, false),
(2013, 'lt$ikkunat$luode$g_ks', 36, false),
(2013, 'lt$ikkunat$pohjoinen$U', 37, false),
(2013, 'lt$ikkunat$pohjoinen$g_ks', 38, false),

(2013, 'lt$ilmanvaihto$tyyppi_id', 39, false),
(2013, 'lt$ilmanvaihto$kuvaus_fi', 40, false),
(2013, 'lt$ilmanvaihto$kuvaus_sv', 41, false),
(2013, 'lt$ilmanvaihto$paaiv$tulo', 42, false),
(2013, 'lt$ilmanvaihto$paaiv$poisto', 43, false),
(2013, 'lt$ilmanvaihto$erillispoistot$tulo', 44, false),
(2013, 'lt$ilmanvaihto$erillispoistot$poisto', 45, false),
(2013, 'lt$ilmanvaihto$ivjarjestelma$tulo', 46, false),
(2013, 'lt$ilmanvaihto$ivjarjestelma$poisto', 47, false),

(2013, 'lt$lammitys$lammitysmuoto_1$id', 48, false),
(2013, 'lt$lammitys$lammitysmuoto_1$kuvaus_fi', 49, false),
(2013, 'lt$lammitys$lammitysmuoto_1$kuvaus_sv', 50, false),
(2013, 'lt$lammitys$lammitysmuoto_2$kuvaus_fi', 51, false),
(2013, 'lt$lammitys$lammitysmuoto_2$kuvaus_sv', 52, false),
(2013, 'lt$lammitys$lammonjako$id', 53, false),
(2013, 'lt$lammitys$lammonjako$kuvaus_fi', 54, false),
(2013, 'lt$lammitys$lammonjako$kuvaus_sv', 55, false),
(2013, 'lt$lammitys$tilat_ja_iv$jaon_hyotysuhde', 56, false),
(2013, 'lt$lammitys$tilat_ja_iv$apulaitteet', 57, false),
(2013, 'lt$lammitys$lammin_kayttovesi$jaon_hyotysuhde', 58, false),

(2013, 'lt$lkvn_kaytto$ominaiskulutus', 59, false),
(2013, 'lt$lkvn_kaytto$lammitysenergian_nettotarve', 60, false)

on conflict (column_name, versio) do update set
  ordinal = excluded.ordinal,
  bypass_allowed = excluded.bypass_allowed;

insert into validation_sisainen_kuorma (
  versio, kayttotarkoitusluokka_id,

  valaistus$kayttoaste,
  valaistus$lampokuorma,
  kuluttajalaitteet$kayttoaste,
  kuluttajalaitteet$lampokuorma,
  henkilot$kayttoaste,
  henkilot$lampokuorma)
values
(2013, 1, 0.1, 8, 0.6, 3, 0.6, 2),
(2013, 2, 0.1, 8, 0.6, 3, 0.6, 2),
(2013, 3, 0.1, 11, 0.6, 4, 0.6, 3),
(2013, 4, 0.65, 12, 0.65, 12, 0.65, 5),
(2013, 5, 1, 19, 1, 1, 1, 2),
(2013, 6, 0.3, 14, 0.3, 4, 0.3, 4),
(2013, 7, 0.6, 18, 0.6, 8, 0.6, 14),
(2013, 8, 0.5, 12, 0.5, 0, 0.5, 5),
(2013, 10, 0.5, 12, 0.5, 0, 0.5, 5),
(2013, 9, 0.6, 9, 0.6, 9, 0.6, 8)
on conflict (kayttotarkoitusluokka_id, versio) do update
  set valaistus$kayttoaste = excluded.valaistus$kayttoaste,
      valaistus$lampokuorma = excluded.valaistus$lampokuorma,
      kuluttajalaitteet$kayttoaste = excluded.kuluttajalaitteet$kayttoaste,
      kuluttajalaitteet$lampokuorma = excluded.kuluttajalaitteet$lampokuorma,
      henkilot$kayttoaste = excluded.henkilot$kayttoaste,
      henkilot$lampokuorma = excluded.henkilot$lampokuorma;
