call create_classification('perusparannuspassi_uusiutuva_energia'::name);
call create_classification('perusparannuspassi_jaahdytys'::name);
call create_classification('perusparannuspassi_liittymismahdollisuus'::name);

call create_classification('toimenpide_ehdotus'::name);
create table perusparannuspassi_vaihe_toimenpide_ehdotus
(
    perusparannuspassi_id integer not null,
    vaihe_nro             integer not null,
    toimenpide_ehdotus_id integer not null references toimenpide_ehdotus(id),
    ordinal               integer not null,
    foreign key (perusparannuspassi_id, vaihe_nro)
        references perusparannuspassi_vaihe(perusparannuspassi_id, vaihe_nro)
);

alter table perusparannuspassi
    add column ppt$havainnointikaynti date,
    add column ppt$passin_esittely date,
    add column ppt$tayttaa_aplus_vaatimukset boolean not null default false,
    add column ppt$tayttaa_a0_vaatimukset boolean not null default false,
    add column rpt$ulkoseinat_ehdotettu_taso numeric,
    add column rpt$ylapohja_ehdotettu_taso numeric,
    add column rpt$alapohja_ehdotettu_taso numeric,
    add column rpt$ikkunat_ehdotettu_taso numeric,
    add column rpt$paalammitysjarjestelma_ehdotettu_taso integer references lammitysmuoto(id),
    add column rpt$ilmanvaihto_ehdotettu_taso integer references ilmanvaihtotyyppi(id),
    add column rpt$uusiutuva_energia_ehdotettu_taso integer references perusparannuspassi_uusiutuva_energia(id),
    add column rpt$jaahdytys_ehdotettu_taso integer references perusparannuspassi_jaahdytys(id),
    add column rpt$mahdollisuus_liittya_energiatehokkaaseen integer references perusparannuspassi_liittymismahdollisuus(id),
    add column t$kaukolampo_hinta numeric,
    add column t$sahko_hinta numeric,
    add column t$uusiutuvat_pat_hinta numeric,
    add column t$fossiiliset_pat_hinta numeric,
    add column t$kaukojaahdytys_hinta numeric,
    add column t$lisatiedot_fi text,
    add column t$lisatiedot_sv text;


alter table perusparannuspassi_vaihe
    add column tp$toimenpideseloste_fi text,
    add column tp$toimenpideseloste_sv text,
    add column t$vaiheen_alku_pvm date,
    add column t$vaiheen_loppu_pvm date,
    add column t$ostoenergian_tarve_kaukolampo numeric,
    add column t$ostoenergian_tarve_sahko numeric,
    add column t$ostoenergian_tarve_uusiutuvat_pat numeric,
    add column t$ostoenergian_tarve_fossiiliset_pat numeric,
    add column t$ostoenergian_tarve_kaukojaahdytys numeric,
    add column t$uusiutuvan_energian_kokonaistuotto numeric,
    add column t$uusiutuvan_energian_hyodynnetty_osuus numeric,
    add column t$toteutunut_ostoenergia_kaukolampo numeric,
    add column t$toteutunut_ostoenergia_sahko numeric,
    add column t$toteutunut_ostoenergia_uusiutuvat_pat numeric,
    add column t$toteutunut_ostoenergia_fossiiliset_pat numeric,
    add column t$toteutunut_ostoenergia_kaukojaahdytys numeric;