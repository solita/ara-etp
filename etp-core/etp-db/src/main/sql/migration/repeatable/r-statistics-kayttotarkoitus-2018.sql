
insert into stat_ktluokka_alaktluokka
  (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio)
select 1, id, versio
from alakayttotarkoitusluokka
where versio = 2018 and kayttotarkoitusluokka_id = 1 and id not in ('AK2', 'RT')
on conflict do nothing;

insert into stat_ktluokka_alaktluokka
  (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio)
select 2, id, versio
from alakayttotarkoitusluokka
where versio = 2018 and kayttotarkoitusluokka_id = 1 and id in ('AK2', 'RT')
on conflict do nothing;

insert into stat_ktluokka_alaktluokka
  (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio)
select kayttotarkoitusluokka_id + 1, id, versio
from alakayttotarkoitusluokka
where versio = 2018 and kayttotarkoitusluokka_id > 1
on conflict do nothing;
