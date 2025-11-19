-- Placeholder numeric validation limits for Perusparannuspassi (PPP) main table
-- These are example values and should be reviewed and adjusted based on actual requirements

insert into ppp_validation_numeric_column
  (versio, column_name, warning$min, warning$max, error$min, error$max)
values
-- Tulokset / Energian hinnat (prices in cents/kWh or similar)
(2026, 't$kaukolampo_hinta', 5, 20, 2, 100),
(2026, 't$sahko_hinta', 5, 100, 2, 100),
(2026, 't$uusiutuvat_pat_hinta', 0, 100, 0, 999),
(2026, 't$fossiiliset_pat_hinta', 0, 100, 0, 999),
(2026, 't$kaukojaahdytys_hinta', 0, 100, 0, 999)

on conflict (column_name, versio) do update set
  warning$min = excluded.warning$min,
  warning$max = excluded.warning$max,
  error$min = excluded.error$min,
  error$max = excluded.error$max;

-- Placeholder numeric validation limits for PPP vaihe (phase) table
-- These are example values and should be reviewed and adjusted based on actual requirements
--
-- insert into ppp_vaihe_validation_numeric_column
--   (versio, column_name, warning$min, warning$max, error$min, error$max)
-- values
-- -- Tulokset / Ostoenergian tarve (energy needs in kWh)
-- (2026, 't$ostoenergian_tarve_kaukolampo', 0, 500000, 0, 999999),
-- (2026, 't$ostoenergian_tarve_sahko', 0, 500000, 0, 999999),
-- (2026, 't$ostoenergian_tarve_uusiutuvat_pat', 0, 500000, 0, 999999),
-- (2026, 't$ostoenergian_tarve_fossiiliset_pat', 0, 500000, 0, 999999),
-- (2026, 't$ostoenergian_tarve_kaukojaahdytys', 0, 500000, 0, 999999),
--
-- -- Tulokset / Uusiutuva energia (renewable energy production/usage)
-- (2026, 't$uusiutuvan_energian_kokonaistuotto', 0, 500000, 0, 999999),
-- (2026, 't$uusiutuvan_energian_hyodynnetty_osuus', 0, 100, 0, 100)
--
-- on conflict (column_name, versio) do update set
--   warning$min = excluded.warning$min,
--   warning$max = excluded.warning$max,
--   error$min = excluded.error$min,
--   error$max = excluded.error$max;
