insert into patevyystaso (id, label_fi, label_sv)
values
(1, 'Perustaso', 'Basnivå'),
(2, 'Ylempi taso', 'Högre nivå')
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv
