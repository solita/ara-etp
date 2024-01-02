
create or replace function fullname(k kayttaja) returns text as $$
begin
  return k.sukunimi || ', ' || k.etunimi;
end;
$$ language plpgsql immutable;