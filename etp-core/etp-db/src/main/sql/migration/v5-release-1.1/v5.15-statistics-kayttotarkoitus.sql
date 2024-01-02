
call create_classification('stat_kayttotarkoitusluokka'::name);

create table stat_ktluokka_alaktluokka (
  stat_kayttotarkoitusluokka_id int not null references stat_kayttotarkoitusluokka (id),
  alakayttotarkoitusluokka_id varchar(4) not null,
  versio int not null,
  valid boolean not null default true,

  foreign key (alakayttotarkoitusluokka_id, versio)
    references alakayttotarkoitusluokka (id, versio),
  primary key (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio)
);

call audit.activate('stat_kayttotarkoitusluokka'::name);
call audit.activate('stat_ktluokka_alaktluokka'::name);