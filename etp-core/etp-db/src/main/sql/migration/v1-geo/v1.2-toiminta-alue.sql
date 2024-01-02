create table toimintaalue (
  id int primary key,
  nuts_code text,
  label_fi text,
  label_sv text,
  ordinal int not null default 0,
  valid boolean not null default true
);
