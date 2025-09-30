call create_classification('perusparannuspassi_paalammitysjarjestelma'::name);

call create_classification('perusparannuspassi_ilmanvaihto'::name);

call create_classification('perusparannuspassi_uusiutuva_energia'::name);

call create_classification('perusparannuspassi_jaahdytys'::name);

call create_classification('perusparannuspassi_toimenpide_ehdotus'::name);

call create_classification('perusparannuspassi_liittymismahdollisuus'::name);

create table perusparannuspassi_vaihe_toimenpide_ehdotus
(
    perusparannuspassi_id   integer not null,
    vaihe_nro               integer not null,
    toimpenpide_ehdotus_id  integer not null references perusparannuspassi_toimenpide_ehdotus(id),
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
    add column rpt$paalammitysjarjestelma_ehdotettu_taso integer references perusparannuspassi_paalammitysjarjestelma(id),
    add column rpt$ilmanvaihto_ehdotettu_taso integer references perusparannuspassi_ilmanvaihto(id),
    add column rpt$uusituva_energia_ehdotettu_taso integer references perusparannuspassi_uusiutuva_energia(id),
    add column rpt$jaahdystys_ehdotettu_taso integer references perusparannuspassi_jaahdytys(id),
    add column rpt$mahdollisuus_liittya_energiatehokkaaseen integer references perusparannuspassi_liittymismahdollisuus(id),
    add column t$kaukolampo_hinta numeric,
    add column t$sahko_hinta numeric,
    add column t$uusiutuvat_pat_hinta numeric,
    add column t$fossiiliset_pat_hinta numeric,
    add column t$kaukojaahdytys_hinta numeric,
    add column t$lisatiedot text;


alter table perusparannuspassi_vaihe
    add column tp$toimenpideseloste text not null default '',
    add column t$vaiheen_alku_pvm date,
    add column t$vaiheen_loppu_pvm date,
    add column t$ostoenergian_tarve_kaukolampo numeric,
    add column t$ostoenergian_tarve_sahko numeric,
    add column t$ostoenergian_tarve_uusiutuvat_pat numeric,
    add column t$ostoenergian_tarve_fossiiliset_pat numeric,
    add column t$ostoenergian_tarve_kaukojäähdytys numeric,
    add column t$uusiutuvan_energian_kokonaistuotto numeric,
    add column t$rakennuksen_hyodyntama_osuus_uusiutuvan_energian_tuotosta numeric,
    add column t$toteutunut_ostoenergia_kaukolampo numeric,
    add column t$toteutunut_ostoenergia_sahko numeric,
    add column t$toteutunut_ostoenergia_uusiutuvat_pat numeric,
    add column t$toteutunut_ostoenergia_fossiiliset_pat numeric,
    add column t$toteutunut_ostoenergia_kaukojaahdytys numeric;