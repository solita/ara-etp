call create_classification('laatija_yritys_tila'::name);

create table laatija_yritys (
  laatija_id int references laatija (id),
  yritys_id int references yritys (id),
  tila_id int default 0 references laatija_yritys_tila (id),
  primary key (laatija_id, yritys_id)
);

call audit.activate('laatija_yritys'::name);
