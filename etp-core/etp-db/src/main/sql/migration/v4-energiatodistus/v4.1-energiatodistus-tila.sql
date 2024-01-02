create table energiatodistustila (
  id int primary key,
  name text not null,
  valid boolean not null default true,
  label_fi text,
  label_sv text,
  description text
);
