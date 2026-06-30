-- name: select-for-ppp-add-requirements
select
    energiatodistus.laatija_id,
    energiatodistus.tila_id,
    energiatodistus.versio
from energiatodistus
where id = :energiatodistus-id;

-- name: select-perusparannuspassi-as-viranomainen
select
    ppp.*,
    et.laatija_id,
    et.tila_id
from perusparannuspassi ppp
    join energiatodistus et on ppp.energiatodistus_id = et.id
where ppp.id = :id
  and ppp.valid = true
  and (et.tila_id in (2, 4) or (et.tila_id = 0 and draft_visible_to_paakayttaja = true));

-- name: select-perusparannuspassi
select
    ppp.*,
    et.laatija_id,
    et.tila_id
from perusparannuspassi ppp
    join energiatodistus et on ppp.energiatodistus_id = et.id
where ppp.id = :id and et.laatija_id = :laatija-id;

-- name: select-perusparannuspassi-vaiheet
select *
from perusparannuspassi_vaihe vaihe
where perusparannuspassi_id = :perusparannuspassi-id
order by vaihe_nro asc;

-- name: update-vaihe!
update perusparannuspassi_vaihe
set
    valid = :valid
where
    vaihe_nro = :vaihe-nro and
    perusparannuspassi_id = :perusparannuspassi-id;

-- name: select-perusparannuspassi-vaihe-toimenpide-ehdotukset
select
    pvte.toimenpide_ehdotus_id as id,
    te.group_id
from perusparannuspassi_vaihe_toimenpide_ehdotus pvte
    join toimenpide_ehdotus te on pvte.toimenpide_ehdotus_id = te.id
where perusparannuspassi_id = :perusparannuspassi-id
  and vaihe_nro = :vaihe-nro
order by pvte.ordinal asc, pvte.toimenpide_ehdotus_id asc;

-- name: delete-perusparannuspassi-vaihe-toimenpide-ehdotukset!
delete
from perusparannuspassi_vaihe_toimenpide_ehdotus
where perusparannuspassi_id = :perusparannuspassi-id
  and vaihe_nro = :vaihe-nro;

-- name: find-by-energiatodistus-id
select
    ppp.*,
    et.laatija_id,
    et.tila_id
from perusparannuspassi ppp
         join energiatodistus et on ppp.energiatodistus_id = et.id
where ppp.energiatodistus_id = :energiatodistus-id
  and et.laatija_id = :laatija-id;

-- name: delete-perusparannuspassi!
update perusparannuspassi set
    valid = false
from energiatodistus et, et_tilat
where perusparannuspassi.id = :id
  and perusparannuspassi.energiatodistus_id = et.id
  and et.tila_id = et_tilat.luonnos;

-- name: delete-perusparannuspassi-vaiheet!
update perusparannuspassi_vaihe
set
    valid = false
where
    perusparannuspassi_id = :perusparannuspassi-id;

-- name: invalidate-perusparannuspassit-by-energiatodistus-id!
update perusparannuspassi
set
    valid = false,
    ppt$havainnointikaynti = null,
    ppt$passin_esittely = null,
    ppt$tayttaa_aplus_vaatimukset = false,
    ppt$tayttaa_a0_vaatimukset = false,
    rpt$ulkoseinat_ehdotettu_taso = null,
    rpt$ylapohja_ehdotettu_taso = null,
    rpt$alapohja_ehdotettu_taso = null,
    rpt$ikkunat_ehdotettu_taso = null,
    rpt$paalammitysjarjestelma_ehdotettu_taso = null,
    rpt$ilmanvaihto_ehdotettu_taso = null,
    rpt$uusiutuva_energia_ehdotettu_taso = null,
    rpt$jaahdytys_ehdotettu_taso = null,
    rpt$mahdollisuus_liittya_energiatehokkaaseen = null,
    t$kaukolampo_hinta = null,
    t$sahko_hinta = null,
    t$uusiutuvat_pat_hinta = null,
    t$fossiiliset_pat_hinta = null,
    t$kaukojaahdytys_hinta = null,
    t$lisatietoja_fi = null,
    t$lisatietoja_sv = null,
    ppt$lisatietoja_fi = null,
    ppt$lisatietoja_sv = null,
    rpt$lisatietoja_fi = null,
    rpt$lisatietoja_sv = null,
    rpt$ulkoovet_ehdotettu_taso = null
where energiatodistus_id = :energiatodistus-id;

-- name: invalidate-perusparannuspassi-vaiheet-by-energiatodistus-id!
update perusparannuspassi_vaihe vaihe
set
    valid = false,
    tp$toimenpideseloste_fi = null,
    tp$toimenpideseloste_sv = null,
    t$vaiheen_alku_pvm = null,
    t$vaiheen_loppu_pvm = null,
    t$ostoenergian_tarve_kaukolampo = null,
    t$ostoenergian_tarve_sahko = null,
    t$ostoenergian_tarve_uusiutuvat_pat = null,
    t$ostoenergian_tarve_fossiiliset_pat = null,
    t$ostoenergian_tarve_kaukojaahdytys = null,
    t$uusiutuvan_energian_kokonaistuotto = null,
    t$uusiutuvan_energian_hyodynnetty_osuus = null,
    t$toteutunut_ostoenergia_kaukolampo = null,
    t$toteutunut_ostoenergia_sahko = null,
    t$toteutunut_ostoenergia_uusiutuvat_pat = null,
    t$toteutunut_ostoenergia_fossiiliset_pat = null,
    t$toteutunut_ostoenergia_kaukojaahdytys = null
from perusparannuspassi ppp
where vaihe.perusparannuspassi_id = ppp.id
  and ppp.energiatodistus_id = :energiatodistus-id;

-- name: select-ppp-numeric-validations
select column_name, warning$min, warning$max, error$min, error$max
from ppp_validation_numeric_column where versio = :versio;

-- name: select-ppp-required-columns
select column_name
from ppp_validation_required_column
where versio = :versio and valid and not (bypass_allowed and :bypass-validation)
order by ordinal asc;

-- name: select-ppp-vaihe-numeric-validations
select column_name, warning$min, warning$max, error$min, error$max
from ppp_vaihe_validation_numeric_column where versio = :versio;

-- name: select-ppp-vaihe-required-columns
select column_name
from ppp_vaihe_validation_required_column
where versio = :versio and valid and not (bypass_allowed and :bypass-validation)
order by ordinal asc;
