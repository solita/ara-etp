call create_classification('yritystype'::name);
call audit.activate('yritystype'::name);

-- The record used as default value is immediately inserted.
-- See the repeatable schema for up-to-date contents.
insert into yritystype (id, label_fi, label_sv, ordinal)
values (1, 'Elinkeinoelämä (03)', 'Elinkeinoelämä (03) (sv)', 4);

alter table yritys add column type_id int not null default 1 references yritystype(id);
