-- name: select-e-luokka-counts
SELECT e.versio,
       e.t$e_luokka e_luokka,
       count(1)
FROM energiatodistus e
LEFT JOIN postinumero p ON e.pt$postinumero = p.id
LEFT JOIN kunta k ON p.kunta_id = k.id
LEFT JOIN toimintaalue t ON k.toimintaalue_id = t.id
WHERE e.tila_id = 2
AND e.voimassaolo_paattymisaika > now()
AND (:keyword::text IS NULL
     OR e.pt$postinumero::text = ltrim(:keyword, '0')
     OR k.label_fi ILIKE :keyword
     OR k.label_sv ILIKE :keyword
     OR t.label_fi ILIKE :keyword
     OR t.label_sv ILIKE :keyword)
AND (:kayttotarkoitus-id::int IS NULL OR (e.pt$kayttotarkoitus, e.versio) IN
       (select alakayttotarkoitusluokka_id, versio from stat_ktluokka_alaktluokka
        where stat_kayttotarkoitusluokka_id = :kayttotarkoitus-id))
AND (:valmistumisvuosi-min::numeric IS NULL OR e.pt$valmistumisvuosi >= :valmistumisvuosi-min)
AND (:valmistumisvuosi-max::numeric IS NULL OR e.pt$valmistumisvuosi <= :valmistumisvuosi-max)
AND (:lammitetty-nettoala-min::numeric IS NULL OR e.lt$lammitetty_nettoala >= :lammitetty-nettoala-min)
AND (:lammitetty-nettoala-max::numeric IS NULL OR e.lt$lammitetty_nettoala <= :lammitetty-nettoala-max)
AND (:versio::int IS NULL OR e.versio = :versio)
AND (:laatimisvaihe::int IS NULL
     OR (:laatimisvaihe = 1 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = true)
           OR (e.versio IN (2018, 2026) AND e.pt$laatimisvaihe IN (0, 1))
         ))
     OR (:laatimisvaihe = 2 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = false)
           OR (e.versio = 2018 AND e.pt$laatimisvaihe IN (2))
           OR (e.versio = 2026 AND e.pt$laatimisvaihe IN (2, 3, 4))
         ))
    )
GROUP BY e.versio, e.t$e_luokka;

-- name: select-lammitys-ilmanvaihto-counts
SELECT e.versio,
       e.lt$lammitys$lammitysmuoto_1$id lammitysmuoto_id,
       e.lt$ilmanvaihto$tyyppi_id ilmanvaihtotyyppi_id,
       count(1)
FROM energiatodistus e
LEFT JOIN postinumero p ON e.pt$postinumero = p.id
LEFT JOIN kunta k ON p.kunta_id = k.id
LEFT JOIN toimintaalue t ON k.toimintaalue_id = t.id
WHERE e.tila_id = 2
AND e.voimassaolo_paattymisaika > now()
AND e.allekirjoitusaika >= '2021-01-01'::timestamp
AND (:keyword::text IS NULL
     OR e.pt$postinumero::text = ltrim(:keyword, '0')
     OR k.label_fi ILIKE :keyword
     OR k.label_sv ILIKE :keyword
     OR t.label_fi ILIKE :keyword
     OR t.label_sv ILIKE :keyword)
AND (:kayttotarkoitus-id::int IS NULL OR (e.pt$kayttotarkoitus, e.versio) IN
       (select alakayttotarkoitusluokka_id, versio from stat_ktluokka_alaktluokka
        where stat_kayttotarkoitusluokka_id = :kayttotarkoitus-id))
AND (:valmistumisvuosi-min::numeric IS NULL OR e.pt$valmistumisvuosi >= :valmistumisvuosi-min)
AND (:valmistumisvuosi-max::numeric IS NULL OR e.pt$valmistumisvuosi <= :valmistumisvuosi-max)
AND (:lammitetty-nettoala-min::numeric IS NULL OR e.lt$lammitetty_nettoala >= :lammitetty-nettoala-min)
AND (:lammitetty-nettoala-max::numeric IS NULL OR e.lt$lammitetty_nettoala <= :lammitetty-nettoala-max)
AND (:versio::int IS NULL OR e.versio = :versio)
AND (:laatimisvaihe::int IS NULL
     OR (:laatimisvaihe = 1 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = true)
           OR (e.versio IN (2018, 2026) AND e.pt$laatimisvaihe IN (0, 1))
         ))
     OR (:laatimisvaihe = 2 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = false)
           OR (e.versio = 2018 AND e.pt$laatimisvaihe IN (2))
           OR (e.versio = 2026 AND e.pt$laatimisvaihe IN (2, 3, 4))
         ))
    )
GROUP BY GROUPING SETS ((e.versio, e.lt$lammitys$lammitysmuoto_1$id), (e.versio, e.lt$ilmanvaihto$tyyppi_id));

-- name: select-e-luku-statistics
SELECT round(avg(e.t$e_luku), 2) avg,
       percentile_cont(0.5) WITHIN GROUP (ORDER BY e.t$e_luku) AS median,
       percentile_cont(0.15) WITHIN GROUP (ORDER BY e.t$e_luku) percentile_15,
       percentile_cont(0.85) WITHIN GROUP (ORDER BY e.t$e_luku) percentile_85
FROM energiatodistus e
LEFT JOIN postinumero p ON e.pt$postinumero = p.id
LEFT JOIN kunta k ON p.kunta_id = k.id
LEFT JOIN toimintaalue t ON k.toimintaalue_id = t.id
WHERE e.versio = :versio
AND e.tila_id = 2
AND e.voimassaolo_paattymisaika > now()
AND (:keyword::text IS NULL
     OR e.pt$postinumero::text = ltrim(:keyword, '0')
     OR k.label_fi ILIKE :keyword
     OR k.label_sv ILIKE :keyword
     OR t.label_fi ILIKE :keyword
     OR t.label_sv ILIKE :keyword)
AND (:kayttotarkoitus-id::int IS NULL OR (e.pt$kayttotarkoitus, e.versio) IN
       (select alakayttotarkoitusluokka_id, versio from stat_ktluokka_alaktluokka
        where stat_kayttotarkoitusluokka_id = :kayttotarkoitus-id))
AND (:valmistumisvuosi-min::numeric IS NULL OR e.pt$valmistumisvuosi >= :valmistumisvuosi-min)
AND (:valmistumisvuosi-max::numeric IS NULL OR e.pt$valmistumisvuosi <= :valmistumisvuosi-max)
AND (:lammitetty-nettoala-min::numeric IS NULL OR e.lt$lammitetty_nettoala >= :lammitetty-nettoala-min)
AND (:lammitetty-nettoala-max::numeric IS NULL OR e.lt$lammitetty_nettoala <= :lammitetty-nettoala-max)
AND (:laatimisvaihe::int IS NULL
     OR (:laatimisvaihe = 1 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = true)
           OR (e.versio IN (2018, 2026) AND e.pt$laatimisvaihe IN (0, 1))
         ))
     OR (:laatimisvaihe = 2 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = false)
           OR (e.versio = 2018 AND e.pt$laatimisvaihe IN (2))
           OR (e.versio = 2026 AND e.pt$laatimisvaihe IN (2, 3, 4))
         ))
    );

-- name: select-common-averages
SELECT avg(e.lt$rakennusvaippa$ilmanvuotoluku) ilmanvuotoluku,
       avg(e.lt$rakennusvaippa$ulkoseinat$u) ulkoseinat_u,
       avg(e.lt$rakennusvaippa$ylapohja$u) ylapohja_u,
       avg(e.lt$rakennusvaippa$alapohja$u) alapohja_u,
       avg(e.lt$rakennusvaippa$ikkunat$u) ikkunat_u,
       avg(e.lt$rakennusvaippa$ulkoovet$u) ulkoovet_u,
       avg(coalesce(e.lt$lammitys$takka$maara, 0)) takka,
       avg(coalesce(e.lt$lammitys$ilmalampopumppu$maara, 0)) ilmalampopumppu,
       avg(nullif(e.lt$lammitys$tilat_ja_iv$lampokerroin, 0)) tilat_ja_iv_lampokerroin,
       avg(nullif(e.lt$lammitys$lammin_kayttovesi$lampokerroin, 0)) lammin_kayttovesi_lampokerroin,
       avg(nullif(e.lt$ilmanvaihto$lto_vuosihyotysuhde, 0)) lto_vuosihyotysuhde,
       avg(nullif(e.lt$ilmanvaihto$ivjarjestelma$sfp, 0)) ivjarjestelma_sfp
FROM energiatodistus e
LEFT JOIN postinumero p ON e.pt$postinumero = p.id
LEFT JOIN kunta k ON p.kunta_id = k.id
LEFT JOIN toimintaalue t ON k.toimintaalue_id = t.id
WHERE e.tila_id = 2
AND e.voimassaolo_paattymisaika > now()
AND (:keyword::text IS NULL
     OR e.pt$postinumero::text = ltrim(:keyword, '0')
     OR k.label_fi ILIKE :keyword
     OR k.label_sv ILIKE :keyword
     OR t.label_fi ILIKE :keyword
     OR t.label_sv ILIKE :keyword)
AND (:kayttotarkoitus-id::int IS NULL OR (e.pt$kayttotarkoitus, e.versio) IN
       (select alakayttotarkoitusluokka_id, versio from stat_ktluokka_alaktluokka
        where stat_kayttotarkoitusluokka_id = :kayttotarkoitus-id))
AND (:valmistumisvuosi-min::numeric IS NULL OR e.pt$valmistumisvuosi >= :valmistumisvuosi-min)
AND (:valmistumisvuosi-max::numeric IS NULL OR e.pt$valmistumisvuosi <= :valmistumisvuosi-max)
AND (:lammitetty-nettoala-min::numeric IS NULL OR e.lt$lammitetty_nettoala >= :lammitetty-nettoala-min)
AND (:lammitetty-nettoala-max::numeric IS NULL OR e.lt$lammitetty_nettoala <= :lammitetty-nettoala-max)
AND (:versio::int IS NULL OR e.versio = :versio)
AND (:laatimisvaihe::int IS NULL
     OR (:laatimisvaihe = 1 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = true)
           OR (e.versio IN (2018, 2026) AND e.pt$laatimisvaihe IN (0, 1))
         ))
     OR (:laatimisvaihe = 2 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = false)
           OR (e.versio = 2018 AND e.pt$laatimisvaihe IN (2))
           OR (e.versio = 2026 AND e.pt$laatimisvaihe IN (2, 3, 4))
         ))
    );

-- name: select-uusiutuvat-omavaraisenergiat-counts
SELECT count(t$uusiutuvat_omavaraisenergiat$aurinkolampo) FILTER (WHERE t$uusiutuvat_omavaraisenergiat$aurinkolampo > 0) as aurinkolampo,
       count(t$uusiutuvat_omavaraisenergiat$aurinkosahko) FILTER (WHERE t$uusiutuvat_omavaraisenergiat$aurinkosahko > 0) as aurinkosahko,
       count(t$uusiutuvat_omavaraisenergiat$tuulisahko) FILTER (WHERE t$uusiutuvat_omavaraisenergiat$tuulisahko > 0) as tuulisahko,
       count(t$uusiutuvat_omavaraisenergiat$lampopumppu) FILTER (WHERE t$uusiutuvat_omavaraisenergiat$lampopumppu > 0) as lampopumppu,
       count(t$uusiutuvat_omavaraisenergiat$muusahko) FILTER (WHERE t$uusiutuvat_omavaraisenergiat$muusahko > 0) as muusahko,
       count(t$uusiutuvat_omavaraisenergiat$muulampo) FILTER (WHERE t$uusiutuvat_omavaraisenergiat$muulampo > 0) as muulampo
FROM energiatodistus e
LEFT JOIN postinumero p ON e.pt$postinumero = p.id
LEFT JOIN kunta k ON p.kunta_id = k.id
LEFT JOIN toimintaalue t ON k.toimintaalue_id = t.id
WHERE e.versio = :versio
AND e.tila_id = 2
AND e.voimassaolo_paattymisaika > now()
AND (:keyword::text IS NULL
     OR e.pt$postinumero::text = ltrim(:keyword, '0')
     OR k.label_fi ILIKE :keyword
     OR k.label_sv ILIKE :keyword
     OR t.label_fi ILIKE :keyword
     OR t.label_sv ILIKE :keyword)
AND (:kayttotarkoitus-id::int IS NULL OR (e.pt$kayttotarkoitus, e.versio) IN
       (select alakayttotarkoitusluokka_id, versio from stat_ktluokka_alaktluokka
        where stat_kayttotarkoitusluokka_id = :kayttotarkoitus-id))
AND (:valmistumisvuosi-min::numeric IS NULL OR e.pt$valmistumisvuosi >= :valmistumisvuosi-min)
AND (:valmistumisvuosi-max::numeric IS NULL OR e.pt$valmistumisvuosi <= :valmistumisvuosi-max)
AND (:lammitetty-nettoala-min::numeric IS NULL OR e.lt$lammitetty_nettoala >= :lammitetty-nettoala-min)
AND (:lammitetty-nettoala-max::numeric IS NULL OR e.lt$lammitetty_nettoala <= :lammitetty-nettoala-max)
AND (:laatimisvaihe::int IS NULL
     OR (:laatimisvaihe = 1 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = true)
           OR (e.versio IN (2018, 2026) AND e.pt$laatimisvaihe IN (0, 1))
         ))
     OR (:laatimisvaihe = 2 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = false)
           OR (e.versio = 2018 AND e.pt$laatimisvaihe IN (2))
           OR (e.versio = 2026 AND e.pt$laatimisvaihe IN (2, 3, 4))
         ))
    );

-- name: select-gwp-averages
SELECT round(avg(
         coalesce(e.is$hiilijalanjalki$rakennus$rakennustuotteiden_valmistus, 0) +
         coalesce(e.is$hiilijalanjalki$rakennus$kuljetukset_tyomaavaihe, 0) +
         coalesce(e.is$hiilijalanjalki$rakennus$rakennustuotteiden_vaihdot, 0) +
         coalesce(e.is$hiilijalanjalki$rakennus$energiankaytto, 0) +
         coalesce(e.is$hiilijalanjalki$rakennus$purkuvaihe, 0)
       ), 2) rakennus_avg,
       round(avg(
         coalesce(e.is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_valmistus, 0) +
         coalesce(e.is$hiilijalanjalki$rakennuspaikka$kuljetukset_tyomaavaihe, 0) +
         coalesce(e.is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_vaihdot, 0) +
         coalesce(e.is$hiilijalanjalki$rakennuspaikka$energiankaytto, 0) +
         coalesce(e.is$hiilijalanjalki$rakennuspaikka$purkuvaihe, 0)
       ), 2) rakennuspaikka_avg,
       count(1) sample_count
FROM energiatodistus e
LEFT JOIN postinumero p ON e.pt$postinumero = p.id
LEFT JOIN kunta k ON p.kunta_id = k.id
LEFT JOIN toimintaalue t ON k.toimintaalue_id = t.id
WHERE e.versio = 2026
AND e.tila_id = 2
AND e.voimassaolo_paattymisaika > now()
AND e.is$laatimisajankohta IS NOT NULL
AND (:keyword::text IS NULL
     OR e.pt$postinumero::text = ltrim(:keyword, '0')
     OR k.label_fi ILIKE :keyword
     OR k.label_sv ILIKE :keyword
     OR t.label_fi ILIKE :keyword
     OR t.label_sv ILIKE :keyword)
AND (:kayttotarkoitus-id::int IS NULL OR (e.pt$kayttotarkoitus, e.versio) IN
       (select alakayttotarkoitusluokka_id, versio from stat_ktluokka_alaktluokka
        where stat_kayttotarkoitusluokka_id = :kayttotarkoitus-id))
AND (:valmistumisvuosi-min::numeric IS NULL OR e.pt$valmistumisvuosi >= :valmistumisvuosi-min)
AND (:valmistumisvuosi-max::numeric IS NULL OR e.pt$valmistumisvuosi <= :valmistumisvuosi-max)
AND (:lammitetty-nettoala-min::numeric IS NULL OR e.lt$lammitetty_nettoala >= :lammitetty-nettoala-min)
AND (:lammitetty-nettoala-max::numeric IS NULL OR e.lt$lammitetty_nettoala <= :lammitetty-nettoala-max)
AND (:laatimisvaihe::int IS NULL
     OR (:laatimisvaihe = 1 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = true)
           OR (e.versio IN (2018, 2026) AND e.pt$laatimisvaihe IN (0, 1))
         ))
     OR (:laatimisvaihe = 2 AND (
           (e.versio = 2013 AND e.pt$uudisrakennus = false)
           OR (e.versio = 2018 AND e.pt$laatimisvaihe IN (2))
           OR (e.versio = 2026 AND e.pt$laatimisvaihe IN (2, 3, 4))
         ))
    );

-- name: select-count
select count(*) as count
from energiatodistus
where
  tila_id = (select allekirjoitettu from et_tilat) and
  now() < voimassaolo_paattymisaika;
