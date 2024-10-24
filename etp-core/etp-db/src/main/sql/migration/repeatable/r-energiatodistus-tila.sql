insert into energiatodistustila (id, name)
values
(0, 'luonnos'),
(1, 'allekirjoituksessa'),
(2, 'allekirjoitettu'),
(3, 'hylatty'),
(4, 'korvattu'),
(5, 'poistettu'),
(6, 'tuhottu'),
(7, 'vanhentunut')
on conflict (id) do update
  set name = excluded.name;

create or replace view et_tilat as
  select 0 luonnos, 1 allekirjoituksessa, 2 allekirjoitettu,
         3 hylatty, 4 korvattu, 5 poistettu, 6 tuhottu;

create or replace function energiatodistus_tila_audit() returns trigger as
$$
begin
  insert into energiatodistus_tila_history( energiatodistus_id, tila_id )
  values (new.id, new.tila_id);
  return new;
end;
$$
language 'plpgsql';
