insert into ppp_validation_required_column (versio, column_name, ordinal, bypass_allowed)
values
-- Passin perustiedot
(2027, 'ppt$havainnointikaynti', 0, false),
(2027, 'ppt$passin_esittely', 1, false),

-- Rakennuksen perustiedot / Ehdotetut tasot (kaikki paitsi lisätiedot)
(2027, 'rpt$ulkoseinat_ehdotettu_taso', 2, false),
(2027, 'rpt$ylapohja_ehdotettu_taso', 3, false),
(2027, 'rpt$alapohja_ehdotettu_taso', 4, false),
(2027, 'rpt$ikkunat_ehdotettu_taso', 5, false),
(2027, 'rpt$ulkoovet_ehdotettu_taso', 6, false),
(2027, 'rpt$paalammitysjarjestelma_ehdotettu_taso', 7, false),
(2027, 'rpt$ilmanvaihto_ehdotettu_taso', 8, false),
(2027, 'rpt$uusiutuva_energia_ehdotettu_taso', 9, false),
(2027, 'rpt$jaahdytys_ehdotettu_taso', 10, false),
(2027, 'rpt$mahdollisuus_liittya_energiatehokkaaseen', 11, false),
(2027, 'rpt$e_luvun_vahimmaistavoitetaso', 12, false),

-- Tulokset / Energian hinnat (kaikki hinnat)
(2027, 't$kaukolampo_hinta', 13, false),
(2027, 't$sahko_hinta', 14, false),
(2027, 't$uusiutuvat_pat_hinta', 15, false),
(2027, 't$fossiiliset_pat_hinta', 16, false),
(2027, 't$kaukojaahdytys_hinta', 17, false)

on conflict (column_name, versio) do update set
  ordinal = excluded.ordinal,
  bypass_allowed = excluded.bypass_allowed;

update ppp_validation_required_column
set valid = false
where column_name = 'rpt$jaahdytys_ehdotettu_taso';


insert into ppp_vaihe_validation_required_column (versio, column_name, ordinal, bypass_allowed, valid)
values
-- Vaihe / Toimenpideseloste (non-localized version, kept as invalid for backwards compatibility)
(2027, 'tp$toimenpideseloste', 0, false, false),
-- Vaihe / Toimenpideseloste (localized versions - the actual required fields)
(2027, 'tp$toimenpideseloste_fi', 0, false, true),
(2027, 'tp$toimenpideseloste_sv', 1, false, true),

-- Vaihe / Tulokset / Ostoenergian tarve (keskustellaan vielä, mutta lisätään varalta)
(2027, 't$ostoenergian_tarve_kaukolampo', 2, false, true),
(2027, 't$ostoenergian_tarve_sahko', 3, false, true),
(2027, 't$ostoenergian_tarve_uusiutuvat_pat', 4, false, true),
(2027, 't$ostoenergian_tarve_fossiiliset_pat', 5, false, true),
(2027, 't$ostoenergian_tarve_kaukojaahdytys', 6, false, true),

-- Vaihe / Tulokset / Uusiutuva energia
(2027, 't$uusiutuvan_energian_kokonaistuotto', 7, false, true),
(2027, 't$uusiutuvan_energian_hyodynnetty_osuus', 8, false, true)

on conflict (column_name, versio) do update set
  ordinal = excluded.ordinal,
  bypass_allowed = excluded.bypass_allowed,
  valid = excluded.valid;
