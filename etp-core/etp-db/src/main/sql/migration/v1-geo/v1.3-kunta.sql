create table kunta (
  id int primary key,
  label_fi text,
  label_sv text,
  ordinal int not null default 0,
  valid boolean not null default true,
  toimintaalue_id int not null references toimintaalue(id)
);
