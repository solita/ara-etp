-- name: hard-delete-energiatodistus!
delete
from energiatodistus
where id = :id;

-- name: select-expired-energiatodistus-ids
select id
from energiatodistus
where voimassaolo_paattymisaika < current_date;
