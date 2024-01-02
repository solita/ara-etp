create table postinumero (
  id int primary key,
  label_fi text,
  label_sv text,
  ordinal int not null default 0,
  valid boolean not null default true,
  kunta_id int references kunta(id)
);
