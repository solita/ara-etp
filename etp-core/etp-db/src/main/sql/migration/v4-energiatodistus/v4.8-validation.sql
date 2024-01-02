create table validation_numeric_column (
  column_name text,
  versio int,
  warning$min numeric,
  warning$max numeric,
  error$min numeric,
  error$max numeric,
  valid boolean default true,
  primary key (column_name, versio)
);

create table validation_required_column (
  column_name text,
  versio int,
  ordinal int not null default 0,
  valid boolean default true,
  primary key (column_name, versio)
);

create table validation_sisainen_kuorma (
  kayttotarkoitusluokka_id int,
  versio int,
  henkilot$kayttoaste numeric,
  henkilot$lampokuorma numeric,
  kuluttajalaitteet$kayttoaste numeric,
  kuluttajalaitteet$lampokuorma numeric,
  valaistus$kayttoaste numeric,
  valaistus$lampokuorma numeric,
  primary key (kayttotarkoitusluokka_id, versio),
  foreign key (kayttotarkoitusluokka_id, versio)
    references kayttotarkoitusluokka(id, versio)
);

call audit.activate('validation_numeric_column'::name);
call audit.activate('validation_required_column'::name);
call audit.activate('validation_sisainen_kuorma'::name);