
insert into stat_ktluokka_alaktluokka
  (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio)
select kayttotarkoitusluokka_id, id, versio
from alakayttotarkoitusluokka
where versio = 2013 and kayttotarkoitusluokka_id < 10
on conflict do nothing;

insert into stat_ktluokka_alaktluokka
  (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio)
select 8, id, versio
from alakayttotarkoitusluokka
where versio = 2013 and kayttotarkoitusluokka_id = 10
on conflict do nothing;

insert into stat_ktluokka_alaktluokka
  (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio)
select 10, id, versio
from alakayttotarkoitusluokka
where versio = 2013 and kayttotarkoitusluokka_id = 11
on conflict do nothing;
