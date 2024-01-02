
insert into etp.kayttaja (id, rooli_id, etunimi, sukunimi, email, puhelin)
values
(0, -1, 'database', 'etp', 'database@energiatodistusrekisteri.fi', '0501234567'),
(-1, -1, 'conversion', 'etp', 'conversion@energiatodistusrekisteri.fi', '0501234567'),
(-2, -1, 'laskutus', 'etp', 'laskutus@energiatodistusrekisteri.fi', '0501234567'),
(-3, -1, 'communication', 'etp', 'communication@energiatodistusrekisteri.fi', '0501234567'),
(-4, -1, 'presigned', 'etp', 'presigned@energiatodistusrekisteri.fi', '0501234567'),
(-5, -1, 'aineisto', 'etp', 'aineisto@energiatodistusrekisteri.fi', '0501234567')

on conflict (id) do update set
  etunimi = excluded.etunimi,
  sukunimi = excluded.sukunimi,
  email = excluded.email,
  puhelin = excluded.puhelin;
