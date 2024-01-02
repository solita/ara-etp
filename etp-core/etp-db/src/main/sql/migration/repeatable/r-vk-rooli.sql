insert into vk_rooli  (id, ordinal, label_fi, label_sv)
values
(0, 1, 'Omistaja', 'Omistaja (sv)'),
(1, 2, 'Kiinteistövälittäjä', 'Fastighetsmäklare'),
(2, 3, 'Muu tiedoksisaaja', 'Muu tiedoksisaaja (sv)')
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal;
