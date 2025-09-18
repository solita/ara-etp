insert into patevyystaso (id, label_fi, label_sv, valid)
values
(1, 'Perustaso', 'Basnivå', true),
(2, 'Ylempi taso', 'Högre nivå', true),
(3, 'Perustaso + PPP', 'Basnivå + PPP', true),
(4, 'Ylempi taso + PPP', 'Högre nivå + PPP', true)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  valid = excluded.valid;
