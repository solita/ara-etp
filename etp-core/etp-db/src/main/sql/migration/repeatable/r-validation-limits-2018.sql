
insert into validation_numeric_column
  (versio, column_name, warning$min, warning$max, error$min, error$max)
values
(2018, 'lt$lammitetty_nettoala', 50, 999999, 0.5, 999999),
(2018, 'lt$rakennusvaippa$ilmanvuotoluku', 0.2, 15, 0, 50),
(2018, 'lt$rakennusvaippa$ulkoseinat$ala', 0.1, 999999, 0.1, 999999),
(2018, 'lt$rakennusvaippa$ulkoseinat$U', 0.08, 0.81, 0.05, 2),
(2018, 'lt$rakennusvaippa$ylapohja$U', 0.05, 0.47, 0.03, 2),
(2018, 'lt$rakennusvaippa$alapohja$U', 0.05, 0.60, 0.03, 4),
(2018, 'lt$rakennusvaippa$ikkunat$U', 0.50, 3.10, 0.40, 6.5),
(2018, 'lt$rakennusvaippa$ulkoovet$U', 0.50, 2.20, 0.20, 6.5),
(2018, 'lt$rakennusvaippa$kylmasillat_UA', 0.1, 999999, 0.1, 999999),

(2018, 'lt$ilmanvaihto$paaiv$sfp',            0, 2.5, 0, 10),
(2018, 'lt$ilmanvaihto$paaiv$jaatymisenesto', -10, 5, -20, 10),
(2018, 'lt$ilmanvaihto$erillispoistot$sfp',   0, 2.5, 0, 10),
(2018, 'lt$ilmanvaihto$ivjarjestelma$sfp',    0, 2.5, 0, 10),
(2018, 'lt$ilmanvaihto$lto_vuosihyotysuhde',  0, 0.85, 0, 1),

(2018, 'lt$lammitys$tilat_ja_iv$tuoton_hyotysuhde', 0, 1.01, 0, 1.01),
(2018, 'lt$lammitys$tilat_ja_iv$jaon_hyotysuhde', 0, 1, 0, 1),
(2018, 'lt$lammitys$tilat_ja_iv$lampokerroin', 0, 10, 0, 10),
(2018, 'lt$lammitys$tilat_ja_iv$apulaitteet', 0.5, 3.6, 0, 999999),

(2018, 'lt$lammitys$lammin_kayttovesi$tuoton_hyotysuhde', 0, 1.01, 0, 1.01),
(2018, 'lt$lammitys$lammin_kayttovesi$jaon_hyotysuhde', 0, 1, 0, 1),
(2018, 'lt$lammitys$lammin_kayttovesi$lampokerroin', 0, 10, 0, 10),
(2018, 'lt$lammitys$lammin_kayttovesi$apulaitteet', 0, 1.5, 0, 999999),

(2018, 'lt$lammitys$takka$maara',             0, 10, 0, 100),
(2018, 'lt$lammitys$takka$tuotto',            0, 30000, 0, 300000),
(2018, 'lt$lammitys$ilmalampopumppu$maara',  0, 10, 0, 100),
(2018, 'lt$lammitys$ilmalampopumppu$tuotto', 0, 60000, 0, 600000),

(2018, 'lt$lkvn_kaytto$lammitysenergian_nettotarve', 0, 40, 0, 999999),
(2018, 'lt$lkvn_kaytto$ominaiskulutus',              0, 700, 0, 999999),

(2018, 'lt$jaahdytysjarjestelma$jaahdytyskauden_painotettu_kylmakerroin', 0, 3.5, 0, 30)

on conflict (column_name, versio) do update set
  warning$min = excluded.warning$min,
  warning$max = excluded.warning$max,
  error$min = excluded.error$min,
  error$max = excluded.error$max;

-- ikkunoiden raja-arvot
insert into validation_numeric_column
(versio, column_name, warning$min, warning$max, error$min, error$max)
select
    2018, column_name, 0.5, 3.1, 0.4, 6.5 -- u-arvojen rajat
from information_schema.columns
  where table_schema = 'etp' and table_name = 'energiatodistus' and
        column_name like 'lt$ikkunat$%$u'
union all
select
  2018, column_name, 0.3, 0.85, 0.1, 1.0 -- g-kohtisuora-arvojen rajat
from information_schema.columns
where table_schema = 'etp' and table_name = 'energiatodistus' and
    column_name like 'lt$ikkunat$%$g_ks'
on conflict (column_name, versio) do update set
  warning$min = excluded.warning$min,
  warning$max = excluded.warning$max,
  error$min = excluded.error$min,
  error$max = excluded.error$max;

