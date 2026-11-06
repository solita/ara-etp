-- AE-2871: Energiatodistus version 2026 is renamed to 2027.
--
-- This is a one-time bridge for existing databases. On a fresh database there
-- are no versio 2026 rows at this point (the repeatable migrations run after
-- the versioned ones), so everything below is a no-op there, and the
-- repeatable migrations create the versio 2027 data. Further changes to the
-- classifications and validation configuration belong in the repeatables only.
--
-- Order matters because of foreign keys:
--   1. copy referenced rows (2026 -> 2027), parents first
--   2. move energiatodistus rows and their audit rows to 2027
--   3. delete the obsolete 2026 rows, children first

-- 1. Copy referenced rows, parents before children
insert into kayttotarkoitusluokka (id, versio, label_fi, label_sv, ordinal, valid)
select id, 2027, label_fi, label_sv, ordinal, valid
from kayttotarkoitusluokka
where versio = 2026
on conflict do nothing;

insert into alakayttotarkoitusluokka
  (id, versio, kayttotarkoitusluokka_id, label_fi, label_sv, ordinal, valid)
select id, 2027, kayttotarkoitusluokka_id, label_fi, label_sv, ordinal, valid
from alakayttotarkoitusluokka
where versio = 2026
on conflict do nothing;

insert into stat_ktluokka_alaktluokka
  (stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, versio, valid)
select stat_kayttotarkoitusluokka_id, alakayttotarkoitusluokka_id, 2027, valid
from stat_ktluokka_alaktluokka
where versio = 2026
on conflict do nothing;

insert into validation_required_column
  (column_name, versio, ordinal, valid, bypass_allowed)
select column_name, 2027, ordinal, valid, bypass_allowed
from validation_required_column
where versio = 2026
on conflict do nothing;

insert into validation_numeric_column
  (column_name, versio, warning$min, warning$max, error$min, error$max, valid)
select column_name, 2027, warning$min, warning$max, error$min, error$max, valid
from validation_numeric_column
where versio = 2026
on conflict do nothing;

insert into validation_sisainen_kuorma
  (kayttotarkoitusluokka_id, versio,
   henkilot$kayttoaste, henkilot$lampokuorma,
   kuluttajalaitteet$kayttoaste, kuluttajalaitteet$lampokuorma,
   valaistus$kayttoaste, valaistus$lampokuorma)
select kayttotarkoitusluokka_id, 2027,
       henkilot$kayttoaste, henkilot$lampokuorma,
       kuluttajalaitteet$kayttoaste, kuluttajalaitteet$lampokuorma,
       valaistus$kayttoaste, valaistus$lampokuorma
from validation_sisainen_kuorma
where versio = 2026
on conflict do nothing;

insert into ppp_validation_required_column
  (column_name, versio, ordinal, valid, bypass_allowed)
select column_name, 2027, ordinal, valid, bypass_allowed
from ppp_validation_required_column
where versio = 2026
on conflict do nothing;

insert into ppp_validation_numeric_column
  (column_name, versio, warning$min, warning$max, error$min, error$max, valid)
select column_name, 2027, warning$min, warning$max, error$min, error$max, valid
from ppp_validation_numeric_column
where versio = 2026
on conflict do nothing;

insert into ppp_vaihe_validation_required_column
  (column_name, versio, ordinal, valid, bypass_allowed)
select column_name, 2027, ordinal, valid, bypass_allowed
from ppp_vaihe_validation_required_column
where versio = 2026
on conflict do nothing;

insert into ppp_vaihe_validation_numeric_column
  (column_name, versio, warning$min, warning$max, error$min, error$max, valid)
select column_name, 2027, warning$min, warning$max, error$min, error$max, valid
from ppp_vaihe_validation_numeric_column
where versio = 2026
on conflict do nothing;


-- 2. Move certificates and audit data. Triggers are disabled so that the
-- version change does not generate additional audit events.
alter table energiatodistus disable trigger user;

alter table audit.energiatodistus disable trigger user;

update energiatodistus set versio = 2027 where versio = 2026;

update audit.energiatodistus set versio = 2027 where versio = 2026;

alter table energiatodistus enable trigger user;

alter table audit.energiatodistus enable trigger user;


-- 3. Remove obsolete 2026 configuration, children before parents
delete from stat_ktluokka_alaktluokka where versio = 2026;

delete from validation_sisainen_kuorma where versio = 2026;

delete from validation_numeric_column where versio = 2026;

delete from validation_required_column where versio = 2026;

delete from ppp_vaihe_validation_numeric_column where versio = 2026;

delete from ppp_vaihe_validation_required_column where versio = 2026;

delete from ppp_validation_numeric_column where versio = 2026;

delete from ppp_validation_required_column where versio = 2026;

delete from alakayttotarkoitusluokka where versio = 2026;

delete from kayttotarkoitusluokka where versio = 2026;
