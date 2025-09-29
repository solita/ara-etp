create table ppp_energiatehokkuus_mahdollisuus
(
    id          integer not null primary key,
    label_fi    text,
    label_sv    text,
    valid       boolean default true not null,
    ordinal     integer default 0 not null,
    description text
);

create table ppp_paalammitysjarjestelma
(
    id          integer not null primary key,
    label_fi    text,
    label_sv    text,
    valid       boolean default true not null,
    ordinal     integer default 0 not null,
    description text
);

create table ppp_ilmanvaihto
(
    id          integer not null primary key,
    label_fi    text,
    label_sv    text,
    valid       boolean default true not null,
    ordinal     integer default 0 not null,
    description text
);

create table ppp_uusiutuva_energia
(
    id          integer not null primary key,
    label_fi    text,
    label_sv    text,
    valid       boolean default true not null,
    ordinal     integer default 0 not null,
    description text
);

create table ppp_jaahdytys
(
    id          integer not null primary key,
    label_fi    text,
    label_sv    text,
    valid       boolean default true not null,
    ordinal     integer default 0 not null,
    description text
);

create table ppp_toimenpide_ehdotus
(
    id          integer not null primary key,
    label_fi    text,
    label_sv    text,
    valid       boolean default true not null,
    ordinal     integer default 0 not null,
    description text
);

alter table perusparannuspassi
    add column pp$havainnointikaynti date,
    add column pp$passin_esittely date,
    add column pp$tayttaa_Aplus_vaatimukset boolean not null default false,
    add column pp$tayttaa_A0_vaatimukset boolean not null default false,
    add column rp$ulkoseinat_ehdotettu_taso numeric not null default 0,
    add column rp$ylapohja_ehdotettu_taso numeric not null default 0,
    add column rp$alapohja_ehdotettu_taso numeric not null default 0,
    add column rp$ikkunat_ehdotettu_taso numeric not null default 0,
    add column rp$paalammitysjarjestelma_ehdotettu_taso int not null default 0 references ppp_paalammitysjarjestelma(id),
    add column rp$ilmanvaihto_ehdotettu_taso int not null default 0 references ppp_ilmanvaihto(id),
    add column rp$uusituva_energia_ehdotettu_taso int not null default 0 references ppp_uusiutuva_energia(id),
    add column rp$jaahdystys_ehdotettu_taso int not null default 0 references ppp_jaahdytys(id),
    add column rp$mahdollisuus_liittya_energiatehokkaaseen int not null default 0 references ppp_energiatehokkuus_mahdollisuus(id),
    add column lt$kaukolampo_hinta numeric not null default 0,
    add column lt$sahko_hinta numeric not null default 0,
    add column lt$uusiutuvatPAt_hinta numeric not null default 0,
    add column lt$fossiilisetPAt_hinta numeric not null default 0,
    add column lt$kaukojaahdytys_hinta numeric not null default 0,
    add column lt$lisatiedot text;
;

alter table perusparannuspassi_vaihe
    add column tp$toimenpide_ehdotukset ppp_toimenpide_ehdotus[],
    add column tp$toimenpideseloste text,
    add column lt$vaiheen_alku_pvm date,
    add column lt$vaiheen_loppu_pvm date,
    add column lt$ostoenergian_tarve_kaukolampo integer not null default 0,
    add column lt$ostoenergian_tarve_sahko integer not null default 0,
    add column lt$ostoenergian_tarve_uusiutuvatPAt integer not null default 0,
    add column lt$ostoenergian_tarve_fossiilisetPAt integer not null default 0,
    add column lt$ostoenergian_tarve_kaukojäähdytys integer not null default 0,
    add column lt$uusiutuvan_energian_kokonaistuotto integer not null default 0,
    add column lt$rakennuksen_hyodyntama_osuus_uusiutuvan_energian_tuotosta integer not null default 0,
    add column lt$toteutunut_ostoenergia_kaukolampo integer not null default 0,
    add column lt$toteutunut_ostoenergia_sahko integer not null default 0,
    add column lt$toteutunut_ostoenergia_uusiutuvatPAt integer not null default 0,
    add column lt$toteutunut_ostoenergia_fossiilisetPAt integer not null default 0,
    add column lt$toteutunut_ostoenergia_kaukojaahdytys integer not null default 0;