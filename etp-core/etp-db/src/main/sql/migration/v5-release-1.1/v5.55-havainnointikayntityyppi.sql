call create_classification('havainnointikayntityyppi'::name);

alter table energiatodistus
    add column pt$havainnointikayntityyppi_id integer references havainnointikayntityyppi(id);
