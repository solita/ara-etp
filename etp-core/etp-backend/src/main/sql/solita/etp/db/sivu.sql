-- name: select-all-sivut
select
  id,
  parent_id,
  ordinal,
  published,
  title
from sivu
where :paakayttaja or published;

-- name: select-sivu
select
  id,
  parent_id,
  ordinal,
  published,
  title,
  body
from sivu
where
  id = :id and
  (:paakayttaja or published);

-- name: delete-sivu!
delete from sivu where id = :id;

-- name: insert-sivu<!
insert into sivu (title, body, parent_id, ordinal, published)
values (:title, :body, :parent-id, :ordinal, :published)
returning id;

-- name: select-child-count
select count(*)
from sivu
where
  parent_id is not distinct from :parent-id and
  id is distinct from :id;

-- name: bump-ordinals!
update sivu
set
  ordinal = ordinal + 1
where
  ordinal >= :ordinal and
  parent_id is not distinct from :parent-id;

-- name: compact-ordinals!
update sivu
set ordinal = sivu_ordering.new_ordinal
from (
  select
    id, row_number() over (order by ordinal, id) - 1 as new_ordinal
    from sivu
    where parent_id is not distinct from :parent-id
) as sivu_ordering
where sivu.id = sivu_ordering.id and sivu.ordinal != sivu_ordering.new_ordinal;
