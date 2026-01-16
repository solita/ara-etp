insert into ppp_validation_numeric_column
  (versio, column_name, warning$min, warning$max, error$min, error$max)
values
-- Tulokset / Energian hinnat (prices in cents/kWh or similar)
(2026, 't$kaukolampo_hinta', 5, 20, 2, 100),
(2026, 't$sahko_hinta', 5, 100, 2, 100),
(2026, 't$uusiutuvat_pat_hinta', 0, 100, 0, 999),
(2026, 't$fossiiliset_pat_hinta', 0, 100, 0, 999),
(2026, 't$kaukojaahdytys_hinta', 0, 100, 0, 999),
-- Rakennuksen perustiedot / Ehdotetut tasot (U-arvot)
(2026, 'rpt$ulkoseinat_ehdotettu_taso', 0.08, 0.81, 0.05, 2),
(2026, 'rpt$ylapohja_ehdotettu_taso', 0.05, 0.47, 0.03, 2),
(2026, 'rpt$alapohja_ehdotettu_taso', 0.05, 0.60, 0.03, 4),
(2026, 'rpt$ikkunat_ehdotettu_taso', 0.50, 3.10, 0.40, 6.5),
(2026, 'rpt$ulkoovet_ehdotettu_taso', 0.50, 2.20, 0.20, 6.5)

on conflict (column_name, versio) do update set
  warning$min = excluded.warning$min,
  warning$max = excluded.warning$max,
  error$min = excluded.error$min,
  error$max = excluded.error$max;

insert into ppp_vaihe_validation_numeric_column
  (versio, column_name, warning$min, warning$max, error$min, error$max)
values
(2026, 't$uusiutuvan_energian_hyodynnetty_osuus', 0, 100, 0, 100)
on conflict (column_name, versio) do update set
  warning$min = excluded.warning$min,
  warning$max = excluded.warning$max,
  error$min = excluded.error$min,
  error$max = excluded.error$max;
