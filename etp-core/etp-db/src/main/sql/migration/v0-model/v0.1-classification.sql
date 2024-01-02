create or replace procedure create_classification(table_name name)
language plpgsql
as $$
begin
execute format(
'create table %I (
  id int primary key,
  label_fi text,
  label_sv text,
  valid boolean not null default true,
  ordinal int not null default 0,
  description text
)', table_name);
end
$$;
