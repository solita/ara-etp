call create_classification('havainnointikayntitype'::name);

alter table energiatodistus
    add column pt$havainttointikayntitype integer references havainnointikayntitype(id);
