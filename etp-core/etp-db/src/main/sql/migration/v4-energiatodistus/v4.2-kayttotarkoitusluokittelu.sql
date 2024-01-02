create table kayttotarkoitusluokka (
  id int,
  versio int,
  label_fi text,
  label_sv text,
  ordinal int not null default 0,
  valid boolean not null default true,
  primary key (id, versio)
);
call audit.activate('kayttotarkoitusluokka'::name);

create table alakayttotarkoitusluokka (
  id varchar(4),
  versio int,
  kayttotarkoitusluokka_id int,
  label_fi text,
  label_sv text,
  ordinal int not null default 0,
  valid boolean not null default true,
  primary key (id, versio),
  foreign key (kayttotarkoitusluokka_id, versio)
    references kayttotarkoitusluokka (id, versio)
);
call audit.activate('alakayttotarkoitusluokka'::name);