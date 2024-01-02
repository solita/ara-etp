
-- name: select-countries
select id, label_fi, label_sv, valid from country
where label_fi is not null and label_sv is not null

-- name: select-toiminta-alueet
select id, label_fi, label_sv, valid from toimintaalue

-- name: select-postinumerot
select id, label_fi, label_sv, valid, kunta_id, type_id from postinumero

-- name: select-postinumero-by-id
select id, label_fi, label_sv, valid, kunta_id from postinumero where id = :id

-- name: select-kunnat
select id, label_fi, label_sv, valid, toimintaalue_id from kunta
