create or replace function patevyys_paattymisaika(l laatija) returns timestamp as $$
begin
  return timezone('Europe/Helsinki', l.toteamispaivamaara::timestamp + interval '7 year' + interval '1 day');
end;
$$ language plpgsql immutable;

create or replace function patevyys_voimassa(l laatija) returns boolean as $$
begin
  return transaction_timestamp() between timezone('Europe/Helsinki', l.toteamispaivamaara::timestamp) and patevyys_paattymisaika(l);
end;
$$ language plpgsql immutable;
