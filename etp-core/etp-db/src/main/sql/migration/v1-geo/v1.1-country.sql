-- see https://en.wikipedia.org/wiki/Country_code

create table country (
  id char(2) primary key,
  alpha3 char(3),
  numeric int,
  name text,
  label_fi text,
  label_sv text,
  ordinal int not null default 0,
  valid boolean not null default true
);
