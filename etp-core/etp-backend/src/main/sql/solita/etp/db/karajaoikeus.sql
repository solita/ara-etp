-- name: find-karajaoikeus-name-by-id
select label_fi
from karajaoikeus
where id = :karajaoikeus-id;
