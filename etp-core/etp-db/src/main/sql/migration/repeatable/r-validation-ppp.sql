insert into ppp_validation_required_column (versio, column_name, ordinal, bypass_allowed)
values
-- Passin perustiedot
(2026, 'ppt$havainnointikaynti', 0, false),
(2026, 'ppt$passin_esittely', 1, false),

-- Rakennuksen perustiedot / Ehdotetut tasot (kaikki paitsi lisätiedot)
(2026, 'rpt$ulkoseinat_ehdotettu_taso', 2, false),
(2026, 'rpt$ylapohja_ehdotettu_taso', 3, false),
(2026, 'rpt$alapohja_ehdotettu_taso', 4, false),
(2026, 'rpt$ikkunat_ehdotettu_taso', 5, false),
(2026, 'rpt$paalammitysjarjestelma_ehdotettu_taso', 6, false),
(2026, 'rpt$ilmanvaihto_ehdotettu_taso', 7, false),
(2026, 'rpt$uusiutuva_energia_ehdotettu_taso', 8, false),
(2026, 'rpt$jaahdytys_ehdotettu_taso', 9, false),
(2026, 'rpt$mahdollisuus_liittya_energiatehokkaaseen', 10, false),

-- Tulokset / Energian hinnat (kaikki hinnat)
(2026, 't$kaukolampo_hinta', 11, false),
(2026, 't$sahko_hinta', 12, false),
(2026, 't$uusiutuvat_pat_hinta', 13, false),
(2026, 't$fossiiliset_pat_hinta', 14, false),
(2026, 't$kaukojaahdytys_hinta', 15, false)

on conflict (column_name, versio) do update set
  ordinal = excluded.ordinal,
  bypass_allowed = excluded.bypass_allowed;

insert into ppp_vaihe_validation_required_column (versio, column_name, ordinal, bypass_allowed)
values
-- Vaihe / Toimenpideseloste
(2026, 'tp$toimenpideseloste', 0, false),

-- Vaihe / Tulokset / Ostoenergian tarve (keskustellaan vielä, mutta lisätään varalta)
(2026, 't$ostoenergian_tarve_kaukolampo', 1, false),
(2026, 't$ostoenergian_tarve_sahko', 2, false),
(2026, 't$ostoenergian_tarve_uusiutuvat_pat', 3, false),
(2026, 't$ostoenergian_tarve_fossiiliset_pat', 4, false),
(2026, 't$ostoenergian_tarve_kaukojaahdytys', 5, false),

-- Vaihe / Tulokset / Uusiutuva energia
(2026, 't$uusiutuvan_energian_kokonaistuotto', 6, false),
(2026, 't$uusiutuvan_energian_hyodynnetty_osuus', 7, false)

on conflict (column_name, versio) do update set
  ordinal = excluded.ordinal,
  bypass_allowed = excluded.bypass_allowed;
