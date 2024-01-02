-- https://www.postgresql.org/docs/current/sql-createfunction.html
-- https://dba.stackexchange.com/questions/185044/postgresql-immutable-volatile-stable

create or replace function etp.current_kayttaja_id() returns int as $$
begin
  return split_part(current_setting('application_name'), '@', 1) :: int;
end;
$$ language plpgsql stable;

create or replace function etp.current_service_uri() returns text as $$
begin
  return split_part(current_setting('application_name'), '@', 2) :: text;
end;
$$ language plpgsql stable;