-- name: select-kayttotarkoitusluokat-by-versio
select id, label_fi, label_sv, valid
from kayttotarkoitusluokka where versio = :versio
order by ordinal asc

-- name: select-alakayttotarkoitusluokat-by-versio
select id, kayttotarkoitusluokka_id, label_fi, label_sv, valid
from alakayttotarkoitusluokka where versio = :versio
order by ordinal asc

