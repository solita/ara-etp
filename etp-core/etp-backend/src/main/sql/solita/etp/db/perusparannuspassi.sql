-- name: select-perusparannuspassi
select ppp.*
from perusparannuspassi ppp
where id = :id;

-- name: select-perusparannuspassi-vaiheet
select *
from perusparannuspassi_vaihe vaihe
where perusparannuspassi_id = :perusparannuspassi-id
order by vaihe_nro asc;
